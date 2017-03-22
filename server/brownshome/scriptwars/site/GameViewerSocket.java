package brownshome.scriptwars.site;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import brownshome.scriptwars.server.game.Game;
import brownshome.scriptwars.server.game.GameCreationException;
import brownshome.scriptwars.server.game.GameType;

/**
 * The websocket sends binary data to the web page on every game tick
 **/

@ServerEndpoint("/gameviewer/{gametype}")
public class GameViewerSocket {
	Consumer<ByteBuffer> viewer;
	Game game;
	
	@OnOpen
	public void open(@PathParam("gametype") String gameID, Session session) throws IOException {
		GameType gameType = GameType.getGameType(gameID);
		
		if(gameType == null) {
			session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Invalid ID"));
			return;
		}
		
		try {
			game = gameType.getAvailableGame();
		} catch(GameCreationException e) {
			session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Unable to get the game to watch"));
			return;
		}
		
		synchronized(session) {
			Basic sender = session.getBasicRemote();
			game.getDisplayHandler().addViewer(viewer = data -> {
				synchronized(session) {
					if(session.isOpen())
						try { sender.sendBinary(data); } catch (IOException e) {}
				}
			});
		}
	}
	
	//TODO find out if session.close causes this to fire
	@OnClose
	public void close() {
		game.getDisplayHandler().removeViewer(viewer);
	}
}
