package brownshome.scriptwars.server.connection;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	public Game game;
	
	Player[] connectedPlayers = new Player[256];
	Set<Player> activePlayers = new HashSet<>();
	Set<Player> outstandingPlayers = new HashSet<>();
	
	public void waitForResponses(long cutoffTime) {
		while(!outstandingPlayers.isEmpty() && !checkTimeOut(cutoffTime)) {
			long timeToWait = cutoffTime - System.currentTimeMillis();
			
			if(timeToWait > 0)
				try { game.wait(timeToWait); } catch (InterruptedException e) {}
		}
		
		outstandingPlayers.addAll(activePlayers);
	}
	
	/** 
	 * Sends all data relevant to a game
	 **/
	public void sendData() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[game.getDataSize()]);
		
		if(!game.hasPerPlayerData()) {
			if(game.getData(null, buffer))
				return;
			
			buffer.flip();
		}
		
		for(Player player : activePlayers) {
			if(game.hasPerPlayerData()) {
				buffer.clear();
				if(!game.getData(player, buffer))
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

	/** Gets the number of active players
	 * @return The number of active players currently on the server
	 */
	public int getPlayerCount() {
		return activePlayers.size();
	}

	protected abstract void timeOutPlayer(Player player);

	protected abstract void endGame(Player player);

	protected abstract int getProtocolByte();

	protected abstract void sendData(Player player, ByteBuffer buffer);

	protected abstract void sendError(Player player, String message);

	/**
	 * Makes a player active. If the game cannot accept another player the player is set to a non-active state and a new ID is sent to
	 * them in an error message.
	 * @param player The player to make active
	 */
	protected void makePlayerActive(Player player) {
		if(!game.hasSpaceForPlayer()) {
			//Move player to a new game
			try {
				sendError(player, "That game is full, here is a new ID " + game.getType().getUserID());
			} catch (GameCreationException e) {
				sendError(player, "That game is full and we were unable to generate a new ID");
			}
			
			return;
		}
		
		player.setActive(true);
		activePlayers.add(player);
		game.addPlayer(player);
	}

	/**
	 * Called by the connection implementation when data is recieved from the client. This is
	 * called once per player per tick.
	 */
	protected void incommingData(ByteBuffer passingBuffer, Player player) {
		if(outstandingPlayers.remove(player)) {
			game.processData(passingBuffer, player);
			
			if(outstandingPlayers.isEmpty())
				game.notify(); //Wake the game thread if it is waiting for responses
		}
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
			game.removePlayer(p);
			
			Server.LOG.info("Player " + p.getName() + " timed out.");
		}
		
		outstandingPlayers.clear();
		
		return true;
	}

	private int createPlayer() throws OutOfIDsException {
		for(int i = 0; i < connectedPlayers.length; i++) {
			if(connectedPlayers[i] == null) {
				connectedPlayers[i] = new Player(i);
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
