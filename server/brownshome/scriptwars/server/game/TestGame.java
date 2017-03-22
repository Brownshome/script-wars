package brownshome.scriptwars.server.game;

import java.nio.ByteBuffer;

import brownshome.scriptwars.server.Server;
import brownshome.scriptwars.server.connection.UDPConnectionHandler;

public class TestGame extends Game {
	
	public TestGame() throws OutOfIDsException {
		super(new UDPConnectionHandler(), new DisplayHandler());
	}
	
	boolean end = false;
	
	@Override
	public boolean hasPerPlayerData() {
		return true;
	}

	@Override
	public int getMaximumPlayers() {
		return 32;
	}

	@Override
	public int getTickRate() {
		return 1000;
	}

	@Override
	public void tick() {
		if(end)
			gameEnded();
		
		Server.LOG.info("Game tick");
	}

	@Override
	public int getDataSize() {
		return Integer.BYTES;
	}

	@Override
	public boolean getData(Player player, ByteBuffer data) {
		data.putInt(player.getSlot());
		return true;
	}

	@Override
	public void processData(ByteBuffer data, Player player) {}

	public static String getName() {
		return "Test game";
	}

	public static String getDescription() {
		return "A bare-bones game to test the networking";
	}

	@Override
	public void displayGame(DisplayHandler handler) {
		handler.putGrid(new char[][] {
			{'#', '#', '#', '#', '#'},
			{' ', ' ', 'P', ' ', ' '}
		});
		
		handler.print();
	}

	@Override
	public void stop() {
		end = true;
	}

	@Override
	public void addPlayer(Player player) {
		System.out.println(player.getName() + " joined the server.");
	}
}
