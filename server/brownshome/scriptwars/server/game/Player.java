package brownshome.scriptwars.server.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.temporal.*;
import java.util.Date;
import java.util.function.Function;

import brownshome.scriptwars.server.connection.ConnectionHandler;

/** Holds the identifying information for each connected member.
 * This class is suitable for using as a key in a Map */
public class Player {
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
	
	private static final int TIMEOUT = 3;
	
	private String name = null;
	private boolean isActive = false;
	private int slot;
	private Game<?> game;
	private ConnectionHandler<?> connection;
	private int missedPackets = 0;
	private LocalTime join;
	private volatile int score;
	
	public Player(int slot, ConnectionHandler<?> connectionHandler, Game<?> game) {
		this.slot = slot;
		connection = connectionHandler;
		this.game = game;
	}

	public int getID() {
		return connection.getProtocolByte() << 16 | game.getSlot() << 8 | slot;
	}
	
	public String getTimeJoined() {
		return join.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive() {
		score = 0;
		join = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
		missedPackets = 0;
		this.isActive = true;
	}

	public int getSlot() {
		return slot;
	}

	public void sendData(ByteBuffer buffer) {
		connection.sendData(this, buffer);
	}

	public void timeOut() {
		isActive = false;
		connection.timeOutPlayer(this);
		game.removePlayer(this);
	}

	public void sendError(String string) {
		connection.sendError(this, string);
	}

	public void endGame() {
		isActive = false;
		connection.endGame(this);
		game.removePlayer(this);
	}

	public void droppedPacket() {
		if(missPacket()) {
			timeOut();
		}
	}
	
	private boolean missPacket() {
		return ++missedPackets >= TIMEOUT;
	}

	/** Only ever call this from one thread */
	public void addScore(int i) {
		score++;
	}
	
	public int getScore() {
		return score;
	}

	public boolean isCorrectProtocol(int protocol) {
		return connection.getProtocolByte() == protocol;
	}

	public void firstData(ByteBuffer passingBuffer) {
		short length = passingBuffer.getShort();
		name = new String(passingBuffer.array(), passingBuffer.arrayOffset() + passingBuffer.position(), length, StandardCharsets.UTF_8); //TODO move to utility function?
		game.makePlayerActive(this);
	}
	
	public void incommingData(ByteBuffer passingBuffer) {
		game.incommingData(passingBuffer, this);
	}

	public ConnectionHandler<?> getConnectionHander() {
		return connection;
	}

	public BufferedImage getIcon(Function<String, File> pathTranslator) throws IOException {
		return game.getIcon(this, s -> pathTranslator.apply(game.getType().getName() + "/" + s));
	}
}
