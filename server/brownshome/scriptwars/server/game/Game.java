package brownshome.scriptwars.server.game;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import brownshome.scriptwars.server.Server;
import brownshome.scriptwars.server.connection.*;

/** The interface that all played games must implement. */
public abstract class Game {
	private enum GameState {
		ACTIVE, CLOSING, CLOSED
	}

	private static final ReentrantReadWriteLock activeGamesLock = new ReentrantReadWriteLock();
	/** This may be read and written to from all three threads. All access must use {@link #activeGamesLock} except from single reads */
	private static final Game[] activeGames = new Game[256];

	/** The time the game has to close in millis */
	public static final long CLOSING_GRACE = 30 * 1000l;

	private final Map<Integer, ConnectionHandler> connections = new HashMap<>();
	private final DisplayHandler displayHandler;
	private int slot = -1;
	
	private GameState state = GameState.ACTIVE; //TODO possibility of pre-start phase
	private long timeClosed;
	private final GameType type;
	
	private Set<Player> activePlayers = new HashSet<>();
	private Set<Player> outstandingPlayers = new HashSet<>();

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
	 * @return The maximum size of the data to send in bytes per player.
	 */
	public abstract int getDataSize();
	
	/** Requests the data packet to be sent to a particular player.
	 * 
	 * @param player The player's data that is being requested. This will be null if {@link #hasPerPlayerData()} is false
	 * @param data A {@link java.nio.ByteBuffer ByteBuffer} that the new data is to be written to.
	 */
	public abstract boolean getData(Player player, ByteBuffer data);
	
	/** Called when an incoming packet is recieved. The packet will always be for the correct tick it is recieved.
	 * 
	 * @param data A {@link java.nio.ByteBuffer ByteBuffer} containing the data
	 * @param player The player that the data was recieved from
	 */
	public abstract void processData(ByteBuffer data, Player player);
	
	/** Called after each tick to get data to display on the website.
	 * 
	 * @param handler The {@link brownshome.scriptwars.server.game.DisplayHandler DisplayHandler} that the commands are to be sent to
	 */
	protected abstract void displayGame(DisplayHandler handler);

	public abstract int getPreferedConnectionType();

	protected Game(GameType type) {
		this.displayHandler = new DisplayHandler();
		
		this.type = type;
	}

	/**
	 * Called by the connection implementation when data is recieved from the client. This is
	 * called once per player per tick.
	 */
	public void incommingData(ByteBuffer passingBuffer, Player player) {
		if(outstandingPlayers.remove(player)) {
			processData(passingBuffer, player);
			
			if(outstandingPlayers.isEmpty())
				notify(); //Wake the game thread if it is waiting for responses
		}
	}

	public void addPlayer(Player player) {
		type.signalListUpdate();
	}
	
	/**Called when a player times out from the server
	 * 
	 * @param p
	 */
	public void removePlayer(Player p) {
		activePlayers.remove(p);
		type.signalListUpdate();
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
	public synchronized List<Player> getActivePlayers() {
		return new ArrayList<>(activePlayers);
	}
	
	/**
	 * @param cutoffTime 
	 * @return True if the timout has exceeded.
	 */
	private boolean checkTimeOut(long cutoffTime) {
		if(System.currentTimeMillis() - cutoffTime <= 0) {
			return false;
		}
		
		for(Player p : outstandingPlayers) {
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
		
		for(Player player : activePlayers) {
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
		for(int i = 0; i < activeGames.length; i++) {
			if(activeGames[i] == null) {
				activeGames[i] = this;
				slot = i;
				break;
			}
		}

		if(slot == -1) {
			Server.LOG.log(Level.SEVERE, "Not enough slots to start game \'" + getName() + "\'.");
			throw new OutOfIDsException();
		}
	}
	
	/**
	 * Makes a player active. If the game cannot accept another player the player is set to a non-active state and a new ID is sent to
	 * them in an error message.
	 * @param player The player to make active
	 */
	public void makePlayerActive(Player player) {
		if(!hasSpaceForPlayer()) {
			//Move player to a new game
			try {
				player.sendError("That game is full, here is a new ID " + getType().getUserID());
			} catch (GameCreationException e) {
				player.sendError("That game is full and we were unable to generate a new ID");
			}
			
			return;
		}
		
		player.setActive();
		activePlayers.add(player);
		addPlayer(player);
	}
	
	/** This is the main method that is called from the game thread
	 * 
	 *  This method is syncronized on the game object, making it so that no network opperations can occur while the game is not sleeping
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
					wait(timeToSleep); //keep this the last line. (wait is used to give up this object's monitor). Posible replace this with sleep and used blocks or locks instead
				} catch (InterruptedException e) {
					endGame();
				}
			} 
			
			//Give it 20ms of leeway
			if(timeToSleep < -20) {
				Server.LOG.log(Level.WARNING, "Game \'" + getName() + "\' is ticking too slowly.");
			}
		}
		
		if(state != GameState.CLOSED) {
			state = GameState.CLOSED;
			Server.LOG.log(Level.WARNING, "Game \'" + getName() + "\' failed to close in time.");
		}
		
		activeGamesLock.writeLock().lock();
		try {
			activeGames[slot] = null;
		} finally {
			activeGamesLock.writeLock().unlock();
		}
	}
	
	private void endGame() {
		for(Player player : activePlayers) {
			player.endGame();
		}
	}

	public void start() {
		Thread thread = new Thread(this::gameLoop, getName() + " thread");
		thread.start();
	}
	
	private boolean shouldClose() {
		return state == GameState.CLOSING && System.currentTimeMillis() - timeClosed > CLOSING_GRACE || state == GameState.CLOSED;
	}

	/** Returns a byte used to identify this game. */
	public int getID() {
		return slot;
	}

	public ConnectionHandler getDefaultConnectionHandler() {
		return getConnectionHandler(getPreferedConnectionType());
	}

	public ConnectionHandler getConnectionHandler(int protocolByte) {
		return connections.computeIfAbsent(protocolByte, i -> ConnectionHandler.createConnection(i, this));
	}

	public DisplayHandler getDisplayHandler() {
		return displayHandler;
	}

	public boolean hasSpaceForPlayer() {
		return getPlayerCount() < getMaximumPlayers();
	}

	public GameType getType() {
		return type;
	}
	
	public int getSlot() {
		return slot;
	}

	/**
	 * @return The name of this game to be displayed on the website
	 */
	public static String getName() {
		return "Unnamed Game";
	}

	/**
	 * @return A short description of the game to be displayed on the website.
	 */
	public static String getDescription() {
		return "No description";
	}

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
}
