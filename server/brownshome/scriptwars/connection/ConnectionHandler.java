package brownshome.scriptwars.connection;

import java.nio.ByteBuffer;

import brownshome.scriptwars.game.Game;
import brownshome.scriptwars.game.GameCreationException;

/**
 * This class is a singleton that handles one specific connection type.
 * 
 * Booleans are packed, all other data types are byte aligned.
 * Strings have a short prefixed to them representing the length of the string.
 * 
 * For outgoing packets the first byte is a purpose code, 0 is game data, 1 is disconnect, 2 is timedOut, -1 is server error.
 */
public abstract class ConnectionHandler<CONNECTION> {
	protected abstract void sendRawData(CONNECTION id, ByteBuffer... data);
	
	public abstract int getProtocolByte();
	public abstract void closeConnectionHandler();
	
	public void sendTimeOut(CONNECTION connection) {
		sendRawData(connection, ByteBuffer.wrap(new byte[] {2}));
		closeConnection(connection);
	}

	public void sendEndGame(CONNECTION connection) {
		sendRawData(connection, ByteBuffer.wrap(new byte[] {1}));
		closeConnection(connection);
	}

	public void sendError(CONNECTION connection, String message) {
		sendRawData(connection, ByteBuffer.wrap(new byte[] {-1}), ConnectionUtil.stringToBuffer(message));
		closeConnection(connection);
	}
	
	public void sendData(CONNECTION connection, ByteBuffer data) {
		sendRawData(connection, ByteBuffer.wrap(new byte[] {0}), data);
	}
	
	public static String getInvalidIDError(Game game) {
		try {
			return "That ID is old, or invalid. Here is a now valid ID " + game.getType().getUserID()
					+ " (This may or may not be the same ID)";
		} catch(GameCreationException e)  {
			return "That has not been requested. Unable to genenerate a new one.";
		}
	}
	
	protected void closeConnection(CONNECTION connection) {}
}
