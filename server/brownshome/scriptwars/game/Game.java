package brownshome.scriptwars.game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.logging.Level;

import brownshome.scriptwars.connection.*;
import brownshome.scriptwars.server.Server;

/** The interface that all played games must implement. */
public abstract class Game {
	private enum GameState {
		WAITING, RUNNING, ENDED
	}

	private static final ReentrantReadWriteLock activeGamesLock = new ReentrantReadWriteLock();
	// SYNCHRONIZED ACCESS ON activeGamesLock
	/** This may be read and written to from all three threads. All access must use {@link #activeGamesLock} except from single reads */
	private static final Game[] activeGames = new Game[256];
	private static final IDPool gameIDPool = new IDPool(256);
	// END SYNC
	
	static ReentrantReadWriteLock getActiveGamesLock() {
		return activeGamesLock;
	}

	public static Game getGame(int gameCode) {
		activeGamesLock.readLock().lock();
		try {
			return activeGames[gameCode];
		} finally {
			activeGamesLock.readLock().unlock();
		}
	}
	
	private final DisplayHandler displayHandler;
	private int slot = -1;
	
	private GameState state = GameState.WAITING;
	private long timeClosed;
	private final GameType type;
	
	// SYCHRONIZED ACCESS ON this
	private PlayerIDPool playerIDPool = new PlayerIDPool(256);
	/** ID players lookup */
	private Player<?>[] players = new Player[256];
	/** Holds all players who have yet to send a response this tick */
	private Set<Player<?>> outstandingPlayers = new HashSet<>();
	/** Holds the currently connected players */
	private Set<Player<?>> activePlayers = new LinkedHashSet<>();
	// END SYNC
	
	private volatile boolean sendPlayerScores = false;
	private volatile boolean updatePlayerList = false;
	
	private final boolean isJudgeGame;
	private final int lifeSpan;
	private final int timeout;
	
	/** 
	 * @return If the game sends specific data to each player
	 */
	public abstract boolean hasPerPlayerData();
	
	/**
	 * @return The maximum amount of players that can join this game
	 */
	public abstract int getMaximumPlayers();
	
	/**
	 * @return The time between each tick in milliseconds
	 */
	public abstract int getTickRate();
	
	/**
	 * Runs the tick for the game
	 */
	protected abstract void tick();
	
	/**
	 * Creates an icon of a specific colour
	 * 
	 * @param player The player to get them for
	 * @param pathTranslator The function used to generate files
	 * @return The final icon
	 * @throws IOException If the file cannot be found
	 */
	public abstract BufferedImage getIcon(Player<?> player, Function<String, File> pathTranslator) throws IOException;
	
	/**
	 * @return The maximum size of the data to send in bytes per player.
	 */
	public abstract int getDataSize();
	
	/** Requests the data packet to be sent to a particular player.
	 * 
	 * @param player The player's data that is being requested. This will be null if {@link #hasPerPlayerData()} is false
	 * @param data A {@link java.nio.ByteBuffer ByteBuffer} that the new data is to be written to.
	 */
	public abstract boolean getData(Player<?> player, ByteBuffer data);
	
	/** Called when an incoming packet is received. The packet will always be for the correct tick it is received.
	 * 
	 * @param data A {@link java.nio.ByteBuffer ByteBuffer} containing the data
	 * @param player The player that the data was received from
	 */
	public abstract void processData(ByteBuffer data, Player<?> player);
	
	/** Called after each tick to get data to display on the website. */
	protected abstract void displayGame();

	public abstract ConnectionHandler<?> getPreferedConnectionHandler();

	protected Game(GameType type) {
		isJudgeGame = false;
		lifeSpan = 0;
		timeout = 0;
		this.type = type;
		this.displayHandler = constructDisplayHandler();
	}
	
	/** Used by the judge program */
	protected Game(GameType type, int ticks, int timeout) {
		this.type = type;
		this.displayHandler = null;
		lifeSpan = ticks;
		isJudgeGame = true;
		this.timeout = timeout;
	}

	protected abstract DisplayHandler constructDisplayHandler();
	
	/**
	 * Called by the connection implementation when data is received from the client. This is
	 * called once per player per tick.
	 */
	public void incommingData(ByteBuffer passingBuffer, Player<?> player) {
		if(outstandingPlayers.remove(player)) {
			processData(passingBuffer, player);
			
			if(outstandingPlayers.isEmpty())
				notify(); //Wake the game thread if it is waiting for responses
		}
	}

	/** Generates an ID for this game, the first byte is a
	 * protocol identifier, the next byte is the game ID,
	 * the last byte is a player id. The MSB is zero.
	 * 
	 * If the number is negative there was no free ID to be generated */
	public int getID() {
		try {
			return getPreferedConnectionHandler().getProtocolByte() << 16 | getSlot() << 8 | playerIDPool.request();
		} catch (OutOfIDsException e) {
			return -1;
		}
	}
	
	public int getID(int protocol) {
		try {
			return protocol << 16 | getSlot() << 8 | playerIDPool.request();
		} catch (OutOfIDsException e) {
			return -1;
		}
	}
	
	protected void onPlayerChange() {
		updatePlayerList = true;
		type.signalListUpdate();
	}
	
	/**Called when a player times out from the server
	 * 
	 * @param p
	 */
	public void removePlayer(Player<?> p) {
		if(!hasEnded()) {
			playerIDPool.free(p.getSlot());
			players[p.getSlot()] = null;
			activePlayers.remove(p);
			onPlayerChange();
		}
	}

	public void waitForResponses(long cutoffTime) {
		while(!outstandingPlayers.isEmpty() && !checkTimeOut(cutoffTime)) {
			long timeToWait = cutoffTime - System.currentTimeMillis();
			
			if(timeToWait > 0)
				try { wait(timeToWait); } catch (InterruptedException e) {}
		}
		
		outstandingPlayers.addAll(activePlayers);
	}
	
	//called by playerTable.jsp
	public synchronized List<Player<?>> getActivePlayers() {
		return new ArrayList<>(activePlayers);
	}
	
	/**
	 * @param cutoffTime 
	 * @return True if the timeout has exceeded.
	 */
	private boolean checkTimeOut(long cutoffTime) {
		if(System.currentTimeMillis() - cutoffTime <= 0) {
			return false;
		}
		
		for(Player<?> p : outstandingPlayers) {
			p.droppedPacket();
		}
		
		outstandingPlayers.clear();
		
		return true;
	}

	/** 
	 * Sends all data relevant to a game
	 **/
	public void sendData() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[getDataSize()]);
		
		if(!hasPerPlayerData()) {
			if(getData(null, buffer))
				return;
			
			buffer.flip();
		}
		
		for(Player<?> player : activePlayers) {
			if(hasPerPlayerData()) {
				buffer.clear();
				if(!getData(player, buffer))
					continue;
				
				buffer.flip();
			}
			
			player.sendData(buffer);
		}
	}
	
	/** Gets the number of active players
	 * @return The number of active players currently on the server
	 */
	public synchronized int getPlayerCount() {
		return activePlayers.size();
	}
	
	/** This must be called inside a write locked block */
	void addToSlot() throws OutOfIDsException {
		slot = gameIDPool.request();
		activeGames[slot] = this;
	}
	
	/**
	 * Attempts to add a player to the game. If the game cannot accept another player the player is set to a non-active state and a new ID is sent to
	 * them in an error message.
	 * @param player The player to make active
	 * @throws InvalidIDException If the player ID is taken, old, or invalid
	 */
	public void addPlayer(Player<?> player) throws InvalidIDException {
		if(!isSpaceForPlayer()) {
			//Player will be moved to a new game
			throw new InvalidIDException();
		}
		
		if(!playerIDPool.isRequested(player.getSlot())) {
			throw new InvalidIDException();
		}
		
		playerIDPool.makeActive(player.getSlot());
		players[player.getSlot()] = player;
		
		activePlayers.add(player);
		
		if(state == GameState.WAITING && !isJudgeGame)
			start();
		
		onPlayerChange();
	}
	
	/** This is the main method that is called from the game thread
	 * 
	 *  This method is synchronised on the game object, making it so that no network operations can occur while the game is not sleeping
	 * 
	 *  FOR INTERNAL USE ONLY */
	private void gameLoop() {
		int tickCount = 0;

		while((!isFinite() || tickCount++ < lifeSpan) && !Server.shouldStop()) {
			synchronized(this) {

				long lastTick = System.currentTimeMillis(); //keep this the first line.

				if(!isJudgeGame && activePlayers.stream().allMatch(Player::isServerSide)) {
					break; //End the game, the only bots left are server-side bots
				}

				tick();

				if(getDisplayHandler() != null) displayGame();

				sendData();
				waitForResponses(lastTick + timeout);

				long timeToSleep;

				//Never sleep in a judging game
				while((timeToSleep = lastTick - System.currentTimeMillis() + getTickRate()) > 0 && !isJudgeGame) {
					try {
						wait(timeToSleep); //keep this the last line. (wait is used to give up this object's monitor). Possible replace this with sleep and used blocks or locks instead
					} catch (InterruptedException e) {
						break;
					}
				} 

				//Give it 20ms of lee-way
				if(timeToSleep < -100) {
					Server.LOG.log(Level.WARNING, "Game \'" + getClass().getName() + "\' is ticking too slowly.");
				}
			}
		}

		synchronized(this) {
			endGame();

			activeGamesLock.writeLock().lock();
			try {
				gameIDPool.free(slot);
				activeGames[slot] = null;
			} finally {
				activeGamesLock.writeLock().unlock();
			}
		}
	}
	
	protected boolean isFinite() {
		return isJudgeGame;
	}

	private void endGame() {
		state = GameState.ENDED;
		if(getDisplayHandler() != null) getDisplayHandler().endGame();
		
		Player<?>[] players = activePlayers.toArray(new Player<?>[activePlayers.size()]);
		for(Player<?> player : players) {
			player.endGame();
		}
		
		getType().endGame(this);
	}

	public Thread start() {
		state = GameState.RUNNING;
		Thread thread = new Thread(this::gameLoop, getClass().getName() + " thread");
		thread.setDaemon(true);
		thread.start();
		
		return thread;
	}

	/** Returns a byte used to identify this game. */
	public int getSlot() {
		return slot;
	}

	public DisplayHandler getDisplayHandler() {
		return displayHandler;
	}
	
	public boolean isSpaceForPlayer() {
		return getPlayerCount() < getMaximumPlayers();
	}

	public GameType getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	public <CONNECTION> Player<CONNECTION> getPlayer(int playerCode) {
		assert playerCode >= 0 && playerCode < 256;
		
		return (Player<CONNECTION>) players[playerCode];
	}

	/** Causes the scores to be upload to the clients next round */
	public void flagScores() {
		sendPlayerScores = true;
	}

	public boolean hasEnded() {
		return state == GameState.ENDED;
	}

	public boolean clearScoreFlag() {
		if(!sendPlayerScores) return false;
		
		sendPlayerScores = false;
		return true;
	}

	public boolean clearPlayersFlag() {
		if(!updatePlayerList) return false;
		
		updatePlayerList = false;
		return true;
	}
	
	public void startServerBot(String name) throws UnknownServerBotException, OutOfIDsException {
		BotFunction function = getType().getServerBot(name);

		int ID = getID(MemoryConnectionHandler.instance().getProtocolByte());

		if(ID == -1)
			throw new OutOfIDsException();

		Thread aiThread = new Thread(() -> {
			try {
				function.start(new String[] {String.valueOf(ID)});
			} catch (Exception e) {
				Server.LOG.log(Level.WARNING, "Error in server bot " + name, e);
			}
		}, name + "@" + ID + " AI thread");

		aiThread.setDaemon(true);
		aiThread.start();
	}
}
