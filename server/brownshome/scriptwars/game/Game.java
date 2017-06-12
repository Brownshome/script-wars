package brownshome.scriptwars.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.logging.Level;

import brownshome.scriptwars.connection.*;
import brownshome.scriptwars.server.Server;

/** The interface that all played games must implement. */
public abstract class Game<DISPLAY_HANDLER extends DisplayHandler> {
	private enum GameState {
		ACTIVE, CLOSING, CLOSED
	}

	private static final ReentrantReadWriteLock activeGamesLock = new ReentrantReadWriteLock();
	// SYNCHRONIZED ACCESS ON activeGamesLock
	/** This may be read and written to from all three threads. All access must use {@link #activeGamesLock} except from single reads */
	private static final Game<?>[] activeGames = new Game<?>[256];
	private static final IDPool gameIDPool = new IDPool(256);
	// END SYNC
	
	/** The time the game has to close in millis */
	public static final long CLOSING_GRACE = 30 * 1000l;

	static ReentrantReadWriteLock getActiveGamesLock() {
		return activeGamesLock;
	}

	public static Game<?> getGame(int gameCode) {
		activeGamesLock.readLock().lock();
		try {
			return activeGames[gameCode];
		} finally {
			activeGamesLock.readLock().unlock();
		}
	}
	
	private final DISPLAY_HANDLER displayHandler;
	private int slot = -1;
	
	private GameState state = GameState.ACTIVE; //TODO possibility of pre-start phase
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
	
	/** Called after each tick to get data to display on the website.
	 * 
	 * @param handler The {@link brownshome.scriptwars.game.DisplayHandler DisplayHandler} that the commands are to be sent to
	 */
	protected abstract void displayGame(DISPLAY_HANDLER handler);

	public abstract ConnectionHandler<?> getPreferedConnectionHandler();

	protected Game(GameType type, DISPLAY_HANDLER displayHandler) {
		this.displayHandler = displayHandler;
		this.type = type;
	}

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
	
	protected void onPlayerChange() {
		type.signalListUpdate();
	}
	
	/**Called when a player times out from the server
	 * 
	 * @param p
	 */
	public void removePlayer(Player<?> p) {
		playerIDPool.free(p.getSlot());
		players[p.getSlot()] = null;
		activePlayers.remove(p);
		onPlayerChange();
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
			
			Server.LOG.info("Player " + p.getName() + " timed out.");
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
	 * @throws IllegalArgumentException If the player is not a valid ID
	 */
	public void addPlayer(Player<?> player) throws IllegalArgumentException {
		if(!isSpaceForPlayer()) {
			//Move player to a new game
			try {
				player.sendError("That game is full, here is a new ID " + getType().getUserID());
			} catch (GameCreationException e) {
				player.sendError("That game is full and we were unable to create a new one.");
			}
			
			return;
		}
		
		if(!playerIDPool.isRequested(player.getSlot())) {
			throw new IllegalArgumentException();
		}
		
		playerIDPool.makeActive(player.getSlot());
		players[player.getSlot()] = player;
		
		activePlayers.add(player);
		
		onPlayerChange();
	}
	
	/** This is the main method that is called from the game thread
	 * 
	 *  This method is synchronised on the game object, making it so that no network operations can occur while the game is not sleeping
	 * 
	 *  FOR INTERNAL USE ONLY */
	private synchronized void gameLoop() {
		while(!Server.shouldStop()) {
			long lastTick = System.currentTimeMillis(); //keep this the first line.

			tick();
			
			if(shouldClose()) {
				break;
			}
			
			displayGame(displayHandler);
			sendData();
			waitForResponses(lastTick + getTickRate());
			
			long timeToSleep;

			while((timeToSleep = lastTick - System.currentTimeMillis() + getTickRate()) > 0) {
				try {
					wait(timeToSleep); //keep this the last line. (wait is used to give up this object's monitor). Possible replace this with sleep and used blocks or locks instead
				} catch (InterruptedException e) {
					endGame();
				}
			} 
			
			//Give it 20ms of lee-way
			if(timeToSleep < -100) {
				Server.LOG.log(Level.WARNING, "Game \'" + getClass().getName() + "\' is ticking too slowly.");
			}
		}
		
		if(state != GameState.CLOSED) {
			state = GameState.CLOSED;
			Server.LOG.log(Level.WARNING, "Game \'" + getClass().getName() + "\' failed to close in time.");
		}
		
		activeGamesLock.writeLock().lock();
		try {
			gameIDPool.free(slot);
			activeGames[slot] = null;
		} finally {
			activeGamesLock.writeLock().unlock();
		}
	}
	
	private void endGame() {
		for(Player<?> player : activePlayers) {
			player.endGame();
		}
	}

	public void start() {
		Thread thread = new Thread(this::gameLoop, getClass().getName() + " thread");
		thread.start();
	}
	
	private boolean shouldClose() {
		return state == GameState.CLOSING && System.currentTimeMillis() - timeClosed > CLOSING_GRACE || state == GameState.CLOSED;
	}

	/** Returns a byte used to identify this game. */
	public int getSlot() {
		return slot;
	}

	public DISPLAY_HANDLER getDisplayHandler() {
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
		if(playerCode < 0 || playerCode > 255)
			return null;
		
		return (Player<CONNECTION>) players[playerCode];
	}
}
