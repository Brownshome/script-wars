package brownshome.scriptwars.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import brownshome.scriptwars.connection.ConnectionHandler;
import brownshome.scriptwars.connection.InvalidIDException;
import brownshome.scriptwars.connection.MemoryConnectionHandler;
import brownshome.scriptwars.server.Server;

//This class needs to be in the common package for compilation of uses of the Tank class. Not sure why.
//Any use of this class will result in ClassNotFoundExceptions

/** Holds the identifying information for each connected member.
 * This class is suitable for using as a key in a Map */
public class Player<CONNECTION> {
	public static final Color[] colours = {
		Color.RED, 
		Color.GREEN, 
		Color.CYAN, 
		Color.BLUE, 
		Color.MAGENTA, 
		Color.PINK, 
		Color.YELLOW.darker(), 
		Color.BLACK, 
		Color.DARK_GRAY, 
		Color.ORANGE.darker()
	};
	
	private static final int TIMEOUT = 20;
	
	private final String name;
	private final int slot;
	private int missedPackets = 0;
	
	private final CONNECTION connection;
	private final Game game;
	private final ConnectionHandler<CONNECTION> connectionHandler;
	
	/** The time that this player joined the game */
	private final LocalTime join;
	private boolean isAdded = false;
	
	/** The score of the player in the current game. */
	private volatile int score = 0;
	
	public static <CONNECTION> Player<CONNECTION> getPlayerFromID(int ID) {
		int playerCode = ID & 0xff;
		int gameCode = (ID >> 8) & 0xff;
		
		Game game = Game.getGame(gameCode);
		if(game == null) return null;
		return game.getPlayer(playerCode);
	}
	
	public Player(int ID, String name, ConnectionHandler<CONNECTION> connectionHandler, CONNECTION connection) throws InvalidIDException {
		int protocol = (ID >> 16) & 0xff;
		int playerCode = ID & 0xff;
		int gameCode = (ID >> 8) & 0xff;
	
		game = Game.getGame(gameCode);
		this.connectionHandler = connectionHandler;
		this.name = name;
		this.connection = connection;
		this.join = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
		this.slot = playerCode;
		
		if(game == null) {
			sendError("Invalid game slot");
			throw new InvalidIDException();
		}
		
		if(connectionHandler.getProtocolByte() != protocol) {
			sendInvalidIDError();
			throw new InvalidIDException();
		}
	}

	public int getID() {
		return connectionHandler.getProtocolByte() << 16 | game.getSlot() << 8 | slot;
	}
	
	public String getTimeJoined() {
		return join.toString();
	}
	
	public String getName() {
		return name;
	}

	public int getSlot() {
		return slot;
	}

	public void sendData(ByteBuffer buffer) {
		connectionHandler.sendData(connection, buffer);
	}

	public void timeOut() {
		Server.LOG.info("Player " + name + " timed out.");
		silentTimeOut();
		connectionHandler.sendTimeOut(connection);
	}

	/** Times out the player without sending a packet, used when the connection is terminated externally */
	public void silentTimeOut() {
		removePlayer();
	}
	
	public void sendInvalidIDError() {
		sendError(ConnectionHandler.getInvalidIDError(game));
	}
	
	public void sendError(String string) {
		connectionHandler.sendError(connection, string);
		removePlayer();
	}

	public boolean isServerSide() {
		return connectionHandler instanceof MemoryConnectionHandler;
	}
	
	public void endGame() {
		connectionHandler.sendEndGame(connection);
		removePlayer();
	}

	public void droppedPacket() {
		if(missPacket()) {
			timeOut();
		}
	}
	
	private boolean missPacket() {
		return ++missedPackets >= TIMEOUT;
	}

	/** Only ever call this from one thread, the game thread */
	public void addScore(int i) {
		game.flagScores();
		score = score + i;
	}
	
	public int getScore() {
		return score;
	}

	public boolean isCorrectProtocol(int protocol) {
		return connectionHandler.getProtocolByte() == protocol;
	}

	public void incommingData(ByteBuffer passingBuffer) {
		game.incommingData(passingBuffer, this);
	}

	public ConnectionHandler<CONNECTION> getConnectionHander() {
		return connectionHandler;
	}

	public BufferedImage getIcon(Function<String, File> pathTranslator) throws IOException {
		return game.getIcon(this, s -> pathTranslator.apply(game.getType().getName() + "/" + s));
	}
	
	public CONNECTION getConnection() {
		return connection;
	}

	public Game getGame() {
		return game;
	}

	public void removePlayer() {
		if(isAdded)
			game.removePlayer(this);
		
		isAdded = false;
	}
	
	public void addPlayer() throws InvalidIDException {
		assert !isAdded;
		
		game.addPlayer(this);
		isAdded = true;
	}
}
