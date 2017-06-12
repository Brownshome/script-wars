package brownshome.scriptwars.connection;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import brownshome.scriptwars.game.*;
import brownshome.scriptwars.server.Server;

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
	
	public static String getInvalidIDError(Game<?> game) {
		try {
			return "That has not been requested. Use this one: " + game.getType().getUserID();
		} catch(GameCreationException e)  {
			return "That has not been requested. Unable to genenerate a new one.";
		}
	}
	
	protected void closeConnection(CONNECTION connection) {}
}
