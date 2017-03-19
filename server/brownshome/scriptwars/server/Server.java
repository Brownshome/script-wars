package brownshome.scriptwars.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import brownshome.scriptwars.server.game.GameCreationException;
import brownshome.scriptwars.server.game.GameType;
import brownshome.scriptwars.server.game.TestGame;
import brownshome.scriptwars.server.game.tanks.TankGame;

/** Main entrypoint of the server program. 
 * This class dispatches the loader and the connection handlers. */
public class Server {
	public static final Logger LOG = Logger.getLogger("backendServer");
	
	public static void initialize() {
		try {
			GameType.addDebugType(TestGame.class);
			GameType.addType(TankGame.class);
		} catch (GameCreationException gce) {
			Server.LOG.log(Level.SEVERE, "Improperly built game files.", gce);
		}
	}
	
	public static List<GameType> getGames() {
		return GameType.getGameTypes();
	}
}
