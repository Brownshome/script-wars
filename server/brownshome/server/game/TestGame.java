package brownshome.server.game;

import java.nio.ByteBuffer;

import brownshome.server.Server;

public class TestGame implements Game {
	GameHandler handler;
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
			handler.gameEnded();
		
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

	@Override
	public String getName() {
		return "Test game";
	}

	@Override
	public String getDescription() {
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
	public void setHandler(GameHandler gameHandler) {
		this.handler = gameHandler;
	}

	@Override
	public void addPlayer(Player player) {
		System.out.println(player.getName() + " joined the server.");
	}
}
