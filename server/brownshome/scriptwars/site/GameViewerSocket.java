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
 * 	0 - bulk sync
 * 	1 - delta update
 * 	2 - game table update
 * 
 * 	0: width, height, char data
 * 	1: {char, x, y}
 *  2: No data, get the table via AJAX
 */

@ServerEndpoint("/gameviewer/{gametype}")
public class GameViewerSocket {
	Consumer<ByteBuffer> viewer;
	Runnable updateGameTable;
	
	Game<?> game;
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
		
		updateGameTable = () -> {
			synchronized(session) {
				if(session.isOpen()) {
					try { sender.sendBinary(ByteBuffer.wrap(new byte[] {(byte) 2}));} catch (IOException e) {}
				}
			}
		};
		
		type.onListUpdate(updateGameTable);
		
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
		removeViewer();
		
		if(type != null)
			type.removeOnListUpdate(updateGameTable);
	}
	
	@OnError
	public void error(Throwable t) {
		//This will only be low down errors, nothing we can really do here, just ignore
		
		removeViewer();
		
		if(type != null)
			type.removeOnListUpdate(updateGameTable);
	}
	
	private void removeViewer() {
		if(game != null)
			game.getDisplayHandler().removeViewer(viewer);
	}
}
