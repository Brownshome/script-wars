package brownshome.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import brownshome.server.connection.UDPConnectionHandler;
import brownshome.server.game.DisplayHandler;
import brownshome.server.game.GameHandler;
import brownshome.server.game.OutOfIDsException;
import brownshome.server.game.tanks.TankGame;

/** Main entrypoint of the server program. 
 * This class dispatches the loader and the connection handlers. */
public class Server {
	public static final Logger LOG = Logger.getGlobal();
	
	public static void main(String args[]) throws OutOfIDsException {
		LOG.setLevel(Level.ALL);
		
		GameHandler testGame = new GameHandler(new TankGame(new boolean[][] {
			{true, true, true, true, true, true, true},
			{true, false, false, false, false, false, true},
			{true, false, false, true, false, false, true},
			{true, false, true, true, true, false, true},
			{true, false, false, true, false, false, true},
			{true, false, false, false, false, false, true},
			{true, true, true, true, true, true, true}
		}), new UDPConnectionHandler(), new DisplayHandler());
		testGame.start();
		
		for(int i = 0; i < 2; i++)
			System.out.println(testGame.connectionHandler.getID());
	}
}
