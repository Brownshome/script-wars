package brownshome.scriptwars.server;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import brownshome.scriptwars.server.connection.UDPConnectionHandler;
import brownshome.scriptwars.server.game.*;
import brownshome.scriptwars.server.game.tanks.TankGame;

/** Main entrypoint of the server program. 
 * This class dispatches the loader and the connection handlers. */
public class Server {
	public static final Logger LOG = Logger.getLogger("brownshome.scriptwars.server");
	
	private static volatile boolean stop = false;
	
	public static void initialize() {
		try {
			UDPConnectionHandler.startListenerThread();
			GameType.addType(TankGame.class);
		} catch (GameCreationException gce) {
			Server.LOG.log(Level.SEVERE, "Improperly built game files.", gce);
		}
	}
	
	public static Collection<GameType> getGames() {
		return GameType.getGameTypes();
	}

	public static void shutdown() {
		stop = true;
		UDPConnectionHandler.stop();
	}
	
	public static boolean shouldStop() {
		return stop;
	}
}
