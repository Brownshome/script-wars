package brownshome.scriptwars.site;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.websocket.*;
import javax.websocket.RemoteEndpoint.*;
import javax.websocket.server.*;

import brownshome.scriptwars.server.game.Game;

/**
 * The websocket sends binary data to the web page on every game tick
 **/

@ServerEndpoint("/gameviewer/{game-id}")
public class GameViewerSocket {
	Consumer<ByteBuffer> viewer;
	
	@OnOpen
	public void open(@PathParam("game-id") Integer gameID, Session session) throws IOException {
		Game game = Game.getGame(gameID);
		
		if(game == null) {
			session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Invalid ID"));
			return;
		}
		
		Basic sender = session.getBasicRemote();
		game.getDisplayHandler().addViewer(viewer = data -> {
			synchronized(session) {
				try {
					sender.sendBinary(data);
				} catch (IOException e) {}
			}
		});
	}
	
	//TODO find out if session.close causes this to fire
	@OnClose
	public void close(@PathParam("game-id") Integer gameID) {
		Game game = Game.getGame(gameID);
		
		if(game == null) {
			//Connection is already closing
			return;
		}
		
		game.getDisplayHandler().removeViewer(viewer);
	}
}
