package brownshome.scriptwars.server.game;

import java.nio.ByteBuffer;
import java.util.logging.Level;

import brownshome.scriptwars.server.Server;
import brownshome.scriptwars.server.connection.ConnectionHandler;

/** The interface that all played games must implement. */
public abstract class Game {
	enum GameState {
		ACTIVE, CLOSING, CLOSED
	}

	static Game[] activeGames = new Game[256];

	/** The time the game has to close in millis */
	static final long CLOSING_GRACE = 30 * 1000l;

	private final ConnectionHandler connectionHandler;
	final DisplayHandler displayHandler;
	final int slot;
	
	GameState state = GameState.ACTIVE; //TODO possibility of pre-start phase
	long timeClosed;
	GameType type;
	
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
	public abstract void tick();
	
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
	
	/**Called when a player times out from the server
	 * 
	 * @param p
	 */
	public void removePlayer(Player p) {
		type.signalListUpdate();
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
	
	/** Called after each tick to get data to display on the website.
	 * 
	 * @param handler The {@link brownshome.scriptwars.server.game.DisplayHandler DisplayHandler} that the commands are to be sent to
	 */
	public abstract void displayGame(DisplayHandler handler);

	/**
	 * Called when the game is externally told to close down. This is a signal to start the end process.
	 * The game will have 30 seconds once this is called to call the {@link GameHandler#gameEnded() gameEnded} method.
	 */
	public abstract void stop();

	public void addPlayer(Player player) {
		type.signalListUpdate();
	}
	
	public Game(ConnectionHandler connectionHandler, DisplayHandler displayHandler, GameType type) throws OutOfIDsException {
		int tmp = -1;
		
		for(int i = 0; i < activeGames.length; i++) {
			if(activeGames[i] == null) {
				activeGames[i] = this;
				tmp = i;
				break;
			}
		}
		
		slot = tmp;
		if(slot == -1) {
			Server.LOG.log(Level.SEVERE, "Not enough slots to start game \'" + getName() + "\'.");
			throw new OutOfIDsException();
		}
		
		this.connectionHandler = connectionHandler;
		connectionHandler.game = this;
		this.displayHandler = displayHandler;
		
		this.type = type;
	}
	
	public static Game getGame(int gameCode) {
		return activeGames[gameCode];
	}
	
	/** Causes the game loop to exit. This should be called from the tick method */
	public void gameEnded() {
		state = GameState.CLOSED;
	}
	
	/** This is the main method that is called from the game thread
	 * 
	 *  FOR INTERNAL USE ONLY */
	void gameLoop() {
		while(true) {
			long lastTick = System.currentTimeMillis(); //keep this the first line.

			tick();
			
			if(shouldClose()) {
				break;
			}
			
			displayGame(displayHandler);
			getConnectionHandler().sendData();
			getConnectionHandler().waitForResponses(lastTick + getTickRate());
			
			long timeToSleep = lastTick - System.currentTimeMillis() + getTickRate();

			if(timeToSleep > 0) {
				try {
					Thread.sleep(timeToSleep); //keep this the last line.
				} catch (InterruptedException e) {
					//Used as a shutdown flag.
					stop();
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
		
		activeGames[slot] = null;
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

	public ConnectionHandler getConnectionHandler() {
		return connectionHandler;
	}

	public DisplayHandler getDisplayHandler() {
		return displayHandler;
	}

	public boolean hasSpaceForPlayer() {
		return connectionHandler.getPlayerCount() < getMaximumPlayers();
	}

	public GameType getType() {
		return type;
	}
	
	public int getSlot() {
		return slot;
	}
}
