package brownshome.server.game;

import java.nio.ByteBuffer;

/** The interface that all played games must implement. */
public interface Game {
	/** 
	 * @return If the game sends specific data to each player
	 */
	boolean hasPerPlayerData();
	
	/**
	 * @return The maximum amount of players that can join this game
	 */
	int getMaximumPlayers();
	
	/**
	 * @return The time between each tick in milliseconds
	 */
	int getTickRate();
	
	/**
	 * Runs the tick for the game
	 */
	void tick();
	
	/**
	 * @return The maximum size of the data to send in bytes per player.
	 */
	int getDataSize();
	
	/** Requests the data packet to be sent to a particular player.
	 * 
	 * @param player The player's data that is being requested. This will be null if {@link #hasPerPlayerData()} is false
	 * @param data A {@link java.nio.ByteBuffer ByteBuffer} that the new data is to be written to.
	 */
	boolean getData(Player player, ByteBuffer data);
	
	/** Called when an incoming packet is recieved. The packet will always be for the correct tick it is recieved.
	 * 
	 * @param data A {@link java.nio.ByteBuffer ByteBuffer} containing the data
	 * @param player The player that the data was recieved from
	 */
	void processData(ByteBuffer data, Player player);
	
	/**
	 * @return The name of this game to be displayed on the website
	 */
	String getName();
	
	/**
	 * @return A short description of the game to be displayed on the website.
	 */
	String getDescription();
	
	/** Called after each tick to get data to display on the website.
	 * 
	 * @param handler The {@link brownshome.server.game.DisplayHandler DisplayHandler} that the commands are to be sent to
	 */
	void displayGame(DisplayHandler handler);

	/**
	 * Called when the game is externally told to close down. This is a signal to start the end process.
	 * The game will have 30 seconds once this is called to call the {@link GameHandler#gameEnded() gameEnded} method.
	 */
	void stop();

	void setHandler(GameHandler gameHandler);

	void addPlayer(Player player);
}
