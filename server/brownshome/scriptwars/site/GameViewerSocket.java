package brownshome.scriptwars.site;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.websocket.*;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import brownshome.scriptwars.game.*;

/**
 * The websocket sends binary data to the web page on every game tick.
 * 
 * Message format.
 * Type:
 * 	0 - update game table
 * 	1 - update player table
 * 	2 - disconnect
 * 	3 - score update
 * 
 * 	4 - bulk update
 * 	5 - delta update
 * 
 * 	6 - update player ID map
 * 
 * 	0: width, height, char data
 * 	1: {char, x, y}
 *  2: No data, get the table via AJAX
 */

@ServerEndpoint("/gameviewer/{gametype}")
public class GameViewerSocket {
	Consumer<ByteBuffer> viewer;
	
	Game game;
	GameType type;
	
	@OnMessage
	public void message(Session session, ByteBuffer buffer) throws IOException {
		int slot;
		
		try {
			slot = Byte.toUnsignedInt(buffer.get());
		} catch(BufferUnderflowException bue) {
			session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Not enough data"));
			return;
		}
		
		removeViewer();
		
		if(slot > 255 || (game = Game.getGame(slot)) == null) {
			session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Invalid slot ID"));
			return;
		}
		
		game.getDisplayHandler().addViewer(viewer);
	}
	
	@OnOpen
	public void open(@PathParam("gametype") String gameID, Session session) throws IOException {
		type = GameType.getGameType(gameID);
		if(type == null) {
			session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Invalid game type"));
			return;
		}
		
		Basic sender = session.getBasicRemote();
		
		viewer = data -> {
				synchronized(session) {
					if(session.isOpen())
						try { sender.sendBinary(data); } catch (IOException e) {}
				}
		};
		
		DisplayHandler.addGlobalViewer(viewer);
	}
	
	//TODO find out if session.close causes this to fire
	@OnClose
	public void close() {
		removeViewer();
		DisplayHandler.removeGlobalViewer(viewer);
	}
	
	@OnError
	public void error(Throwable t) {
		//This will only be low down errors, nothing we can really do here, just ignore
		close();
	}
	
	private void removeViewer() {
		if(game != null)
			game.getDisplayHandler().removeViewer(viewer);
	}
}
