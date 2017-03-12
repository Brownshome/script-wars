package brownshome.server.game;

import java.util.*;
import java.util.logging.Level;

import brownshome.server.Server;
import brownshome.server.connection.ConnectionHandler;
import brownshome.server.game.GameHandler.GameState;

public class GameHandler {
	enum GameState {
		ACTIVE, CLOSING, CLOSED
	}

	static GameHandler[] activeGames = new GameHandler[256];

	/** The time the game has to close in millis */
	static final long CLOSING_GRACE = 30 * 1000l;

	public final Game game;
	public final ConnectionHandler connectionHandler;
	
	final DisplayHandler displayHandler;
	final int slot;
	
	GameState state = GameState.ACTIVE; //TODO possibility of pre-start phase
	long timeClosed;
	
	public GameHandler(Game game, ConnectionHandler connectionHandler, DisplayHandler displayHandler) throws OutOfIDsException {
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
			Server.LOG.log(Level.SEVERE, "Not enough slots to start game \'" + game.getName() + "\'.");
			throw new OutOfIDsException();
		}
		
		this.game = game;
		this.connectionHandler = connectionHandler;
		connectionHandler.gameHandler = this;
		this.displayHandler = displayHandler;
		game.setHandler(this);
	}
	
	public static GameHandler getGame(int gameCode) {
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

			game.tick();
			
			if(shouldClose()) {
				break;
			}
			
			game.displayGame(displayHandler);
			connectionHandler.sendData();
			connectionHandler.waitForResponses(lastTick + game.getTickRate());
			
			long timeToSleep = lastTick - System.currentTimeMillis() + game.getTickRate();

			if(timeToSleep > 0) {
				try {
					Thread.sleep(timeToSleep); //keep this the last line.
				} catch (InterruptedException e) {
					//Used as a shutdown flag.
					game.stop();
				}
			} 
			
			//Give it 20ms of leeway
			if(timeToSleep < -20) {
				Server.LOG.log(Level.WARNING, "Game \'" + game.getName() + "\' is ticking too slowly.");
			}
		}
		
		if(state != GameState.CLOSED) {
			state = GameState.CLOSED;
			Server.LOG.log(Level.WARNING, "Game \'" + game.getName() + "\' failed to close in time.");
		}
		
		activeGames[slot] = null;
	}
	
	public void start() {
		Thread thread = new Thread(this::gameLoop, game.getName() + " thread");
		thread.start();
	}
	
	private boolean shouldClose() {
		return state == GameState.CLOSING && System.currentTimeMillis() - timeClosed > CLOSING_GRACE || state == GameState.CLOSED;
	}

	/** Returns a byte used to identify this game. */
	public int getID() {
		return slot;
	}

	public void makePlayerActive(Player player) {
		assert false : "not implemented";
		
	}
}
