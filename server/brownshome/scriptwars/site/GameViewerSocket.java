package brownshome.scriptwars.site;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import brownshome.scriptwars.server.game.Game;

/**
 * The websocket sends binary data to the web page on every game tick
 **/

@ServerEndpoint("/gameviewer/{gametype}")
public class GameViewerSocket {
	Consumer<ByteBuffer> viewer;
	Game game;
	
	@OnMessage
	public void message(@PathParam("gametype") String gameID, Session session, ByteBuffer buffer) throws IOException {
		int slot;
		
		try {
			slot = Byte.toUnsignedInt(buffer.get());
		} catch(BufferUnderflowException bue) {
			session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Not enough data"));
			return;
		}
		
		close();
		
		if(slot > 255 || (game = Game.getGame(slot)) == null) {
			session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Invalid slot ID"));
			return;
		}
		
		game.getDisplayHandler().addViewer(viewer);
	}
	
	@OnOpen
	public void open(@PathParam("gametype") String gameID, Session session) throws IOException {
		Basic sender = session.getBasicRemote();
		viewer = data -> {
				synchronized(session) {
					if(session.isOpen())
						try { sender.sendBinary(data); } catch (IOException e) {}
				}
		};
	}
	
	//TODO find out if session.close causes this to fire
	@OnClose
	public void close() {
		if(game != null)
			game.getDisplayHandler().removeViewer(viewer);
	}
}
