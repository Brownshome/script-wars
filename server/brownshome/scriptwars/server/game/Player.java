package brownshome.scriptwars.server.game;

import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.temporal.*;
import java.util.Date;

import brownshome.scriptwars.server.connection.ConnectionHandler;

/** Holds the identifying information for each connected member.
 * This class is suitable for using as a key in a Map */
public class Player {
	private static final int TIMEOUT = 3;
	
	private String name = null;
	private boolean isActive = false;
	private int slot;
	private Game game;
	private ConnectionHandler connection;
	private int missedPackets = 0;
	private LocalTime join;
	private volatile int score;
	
	public Player(int slot, ConnectionHandler connectionHandler, Game game) {
		this.slot = slot;
		connection = connectionHandler;
		this.game = game;
	}

	public Colour getColour() {
		return Colour.translateToColour(getName());
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

	public void setName(String name) {
		this.name = name;
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
}
