package brownshome.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import brownshome.server.connection.UDPConnectionHandler;
import brownshome.server.game.DisplayHandler;
import brownshome.server.game.GameHandler;
import brownshome.server.game.OutOfIDsException;
import brownshome.server.game.TestGame;

/** Main entrypoint of the server program. 
 * This class dispatches the loader and the connection handlers. */
public class Server {
	public static final Logger LOG = Logger.getGlobal();
	
	public static void main(String args[]) throws OutOfIDsException {
		LOG.setLevel(Level.OFF);
		
		GameHandler testGame = new GameHandler(new TestGame(), new UDPConnectionHandler(), new DisplayHandler());
		testGame.start();
		
		System.out.println(testGame.connectionHandler.getID());
	}
}
