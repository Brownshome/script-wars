package brownshome.scriptwars.server.connection;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
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
 * Booleans are packed, all other data types are byte aligned.
 * Strings have a short prefixed to them representing the length of the string.
 * 
 * For outgoing packets the first byte is a purpose code, 0 is game data, 1 is disconnect, 2 is timedOut, -1 is server error.
 */
public abstract class ConnectionHandler<PLAYER_ID> {
	private static final Map<Integer, Function<Game, ? extends ConnectionHandler>> constructors;
	
	static {
		Map<Integer, Function<Game, ? extends ConnectionHandler>> innerMap = new HashMap<>();
		
		innerMap.put(UDPConnectionHandler.UPD_PROTOCOL_BYTE, UDPConnectionHandler::new);
		innerMap.put(TCPConnectionHandler.TCP_PROTOCOL_BYTE, TCPConnectionHandler::new);
		
		constructors = Collections.unmodifiableMap(innerMap);
	}
	
	public static ConnectionHandler createConnection(int ID, Game game) {
		try {
			return constructors.get(ID).apply(game);
		} catch(NullPointerException npe) {
			throw new IllegalArgumentException("Invalid protocol ID", npe);
		}
	}

	public static Player getPlayerFromID(int ID) throws ProtocolException {
		int protocol = (ID >> 16) & 0xff;
		int playerCode = ID & 0xff;
		int gameCode = (ID >> 8) & 0xff;

		Game game = Game.getGame(gameCode);
		if(game == null) {
			throw new ProtocolException("Invalid ID");
		}

		ConnectionHandler connectionHandler = game.getConnectionHandler(protocol);
		Player player = connectionHandler.getPlayer(playerCode);
		
		if(player == null || !player.isCorrectProtocol(protocol)) {
			throw new ProtocolException("Invalid ID");
		}
		
		return player;
	}
	
	private final Map<Player, PLAYER_ID> playerMappings = new HashMap<>();
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

	public void sendData(Player player, ByteBuffer buffer) {
		sendRawData(playerMappings.get(player), ByteBuffer.wrap(new byte[] {0}), buffer);
	}

	public void timeOutPlayer(Player player) {
		sendRawData(playerMappings.get(player), ByteBuffer.wrap(new byte[] {2}));
		disconnect(player);
	}

	public void endGame(Player player) {
		sendRawData(playerMappings.get(player), ByteBuffer.wrap(new byte[] {1}));
		disconnect(player);
	}

	protected abstract void sendRawData(PLAYER_ID id, ByteBuffer... data);
	
	public abstract int getProtocolByte();

	public void sendError(Player player, String message) {
		sendRawData(playerMappings.get(player), ByteBuffer.wrap(new byte[] {-1}), stringToBuffer(message));
		disconnect(player);
	}

	public static ByteBuffer stringToBuffer(String message) {
		byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
		ByteBuffer result = ByteBuffer.allocate(bytes.length + Short.BYTES);
		result.putShort((short) bytes.length);
		result.put(bytes);
		result.flip();
		
		return result;
	}
	
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

	protected void disconnect(Player player) {
		playerMappings.remove(player);
	}
	
	protected PLAYER_ID getMapping(Player p) {
		return playerMappings.get(p);
	}
	
	protected void putMapping(PLAYER_ID id, Player p) {
		playerMappings.put(p, id);
	}
}
