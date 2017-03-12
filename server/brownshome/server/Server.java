package brownshome.server;

import java.util.logging.Logger;

import brownshome.server.connection.UDPConnectionHandler;
import brownshome.server.game.*;

/** Main entrypoint of the server program. 
 * This class dispatches the loader and the connection handlers. */
public class Server {
	public static final Logger LOG = Logger.getGlobal();
	
	public static void main(String args[]) throws OutOfIDsException {
		GameHandler testGame = new GameHandler(new TestGame(), new UDPConnectionHandler(), new DisplayHandler());
		testGame.start();
		
		System.out.println(testGame.connectionHandler.getID());
	}
}
