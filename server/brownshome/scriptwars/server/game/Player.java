package brownshome.scriptwars.server.game;

import java.nio.ByteBuffer;

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
	
	public Player(int slot, ConnectionHandler connectionHandler, Game game) {
		this.slot = slot;
		connection = connectionHandler;
		this.game = game;
	}

	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		missedPackets = 0;
		this.isActive = isActive;
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
}
