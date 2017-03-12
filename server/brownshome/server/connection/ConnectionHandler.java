package brownshome.server.connection;

import java.nio.ByteBuffer;
import java.util.*;

import brownshome.server.Server;
import brownshome.server.game.*;

/**
 * This class handles the connections to each player and times out players when they take too long.
 * 
 * Booleans are packed, all other data types are byte alligned.
 * Strings have a short prefixed to them representing the length of the string.
 */
public abstract class ConnectionHandler {
	public GameHandler gameHandler;
	
	Player[] connectedPlayers = new Player[256];
	Set<Player> activePlayers = new HashSet<>();
	Set<Player> outstandingPlayers = new HashSet<>();
	
	public synchronized void waitForResponses(long cutoffTime) {
		while(!outstandingPlayers.isEmpty() && !checkTimeOut(cutoffTime)) {
			long timeToWait = cutoffTime - System.currentTimeMillis();
			
			if(timeToWait > 0)
				try { wait(timeToWait); } catch (InterruptedException e) {}
		}
		
		outstandingPlayers.addAll(activePlayers);
	}
	
	/**
	 * @param cutoffTime 
	 * @return True if the timout has exceeded.
	 */
	private boolean checkTimeOut(long cutoffTime) {
		if(System.currentTimeMillis() - cutoffTime <= 0) {
			return false;
		}
		
		for(Player p : outstandingPlayers) {
			activePlayers.remove(p);
			p.setActive(false);
			timeOutPlayer(p);
			
			Server.LOG.info("Player " + p.getName() + " timed out.");
		}
		
		outstandingPlayers.clear();
		
		return true;
	}

	abstract void timeOutPlayer(Player player);
	abstract void endGame(Player player);
	abstract int getProtocolByte();
	abstract void sendData(Player player, ByteBuffer buffer);
	
	public synchronized void sendData() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[gameHandler.game.getDataSize()]);
		
		if(!gameHandler.game.hasPerPlayerData()) {
			if(gameHandler.game.getData(null, buffer))
				return;
			
			buffer.flip();
		}
		
		for(Player player : activePlayers) {
			if(gameHandler.game.hasPerPlayerData()) {
				if(!gameHandler.game.getData(player, buffer))
					continue;
				
				buffer.flip();
			}
			
			sendData(player, buffer);
		}
	}

	/** Generates an ID for this game, the first byte is a
	 * protocol identifier, the next byte is the game ID,
	 * the last byte is a player id. The MSB is zero.
	 * 
	 * If the number is negative there was no free ID to be generated */
	public int getID() {
		try {
			return getProtocolByte() << 16 | gameHandler.getID() << 8 | createPlayer();
		} catch (OutOfIDsException e) {
			return -1;
		}
	}
	
	private int createPlayer() throws OutOfIDsException {
		for(int i = 0; i < connectedPlayers.length; i++) {
			if(connectedPlayers[i] == null) {
				connectedPlayers[i] = new Player(i);
				return i;
			}
		}
		
		throw new OutOfIDsException();
	}
	
	public Player getPlayer(int playerCode) {
		return connectedPlayers[playerCode];
	}
	
	public synchronized void makePlayerActive(Player player) {
		if(activePlayers.size() >= gameHandler.game.getMaximumPlayers())
			throw new IllegalStateException("Game " + gameHandler.game.getName() + " has reached it's maximum player count.");
		
		player.setActive(true);
		activePlayers.add(player);
		gameHandler.game.addPlayer(player);
	}
	
	public synchronized void incommingData(ByteBuffer passingBuffer, Player player) {
		if(outstandingPlayers.remove(player)) {
			gameHandler.game.processData(passingBuffer, player);
			
			if(outstandingPlayers.isEmpty())
				notify(); //Wake the game thread if it is waiting for responses
		}
	}
}
