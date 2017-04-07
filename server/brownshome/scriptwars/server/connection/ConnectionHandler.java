package brownshome.scriptwars.server.connection;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import brownshome.scriptwars.server.Server;
import brownshome.scriptwars.server.game.Game;
import brownshome.scriptwars.server.game.GameCreationException;
import brownshome.scriptwars.server.game.OutOfIDsException;
import brownshome.scriptwars.server.game.Player;

/**
 * This class handles the connections to each player and times out players when they take too long.
 * 
 * Booleans are packed, all other data types are byte alligned.
 * Strings have a short prefixed to them representing the length of the string.
 */
public abstract class ConnectionHandler {
	private static final Map<Integer, Function<Game, ? extends ConnectionHandler>> constructors;
	
	static {
		Map<Integer, Function<Game, ? extends ConnectionHandler>> innerMap = new HashMap<>();
		
		innerMap.put(UDPConnectionHandler.UPD_PROTOCOL_BYTE, UDPConnectionHandler::new);
		
		constructors = Collections.unmodifiableMap(innerMap);
	}
	
	public static ConnectionHandler createConnection(int ID, Game game) {
		try {
			return constructors.get(ID).apply(game);
		} catch(NullPointerException npe) {
			throw new IllegalArgumentException("Invalid protocol ID", npe);
		}
	}

	protected final Game game;
	
	protected ConnectionHandler(Game game) {
		this.game = game;
	}

	private Player[] connectedPlayers = new Player[256];

	/** Generates an ID for this game, the first byte is a
	 * protocol identifier, the next byte is the game ID,
	 * the last byte is a player id. The MSB is zero.
	 * 
	 * If the number is negative there was no free ID to be generated */
	public int getID() {
		try {
			return getProtocolByte() << 16 | game.getID() << 8 | createPlayer();
		} catch (OutOfIDsException e) {
			return -1;
		}
	}

	/**
	 * Gets a player given their playerCode. The code must be in the byte range.
	 * @param playerCode The slot of the connected player.
	 * @return The player, or null if there is no such player
	 */
	public Player getPlayer(int playerCode) {
		return connectedPlayers[playerCode];
	}

	public abstract void sendData(Player player, ByteBuffer buffer);

	public abstract void timeOutPlayer(Player player);

	public abstract void endGame(Player player);

	protected abstract int getProtocolByte();

	public abstract void sendError(Player player, String message);

	private int createPlayer() throws OutOfIDsException {
		for(int i = 0; i < connectedPlayers.length; i++) {
			if(connectedPlayers[i] == null) {
				connectedPlayers[i] = new Player(i, this, game);
				return i;
			}
		}
		
		List<Integer> ids = IntStream.range(0, connectedPlayers.length).boxed().collect(Collectors.toList());
		Collections.shuffle(ids);
		
		for(int i : ids) {
			if(!connectedPlayers[i].isActive()) {
				return i;
			}
		}
		
		throw new OutOfIDsException();
	}
}
