package brownshome.server.game.tanks;

import java.nio.ByteBuffer;
import java.util.Collection;

import brownshome.server.game.DisplayHandler;
import brownshome.server.game.Game;
import brownshome.server.game.GameHandler;
import brownshome.server.game.Player;

/* Each tick shots are moved x spaces. Then tanks shoot. Then tanks move
 
 *^ -> dead
  
 *  -> survive
  ^
 
 > < -> no movement
 
  v
 *  -> survive
  ^
  
  >> -> back tank shoots 
  
 */

public class TankGame implements Game {
	GameHandler handler;
	World world;
	
	public TankGame(boolean[][] map) {
		this.world = new World(map, this);
	}
	
	@Override
	public boolean hasPerPlayerData() {
		return true;
	}

	@Override
	public int getMaximumPlayers() {
		return 8;
	}

	@Override
	public int getTickRate() {
		return 500;
	}

	@Override
	public synchronized void tick() {
		world.rollBackTanks();
		world.moveShots();
		world.fireTanks();
	}

	@Override
	public int getDataSize() {
		return 5 + getMaximumPlayers() * 2 + world.getDataSize();
	}

	/**
	 * 
	 * byte: 0/1 alive or dead
	 * byte: x
	 * byte: y
	 * byte: width
	 * byte: height
	 * X*boolean: wall array
	 * x*{
	 * 	byte: x
	 * 	byte: y
	 * }
	 */
	@Override
	public synchronized boolean getData(Player player, ByteBuffer data) {
		boolean isAlive = world.isAlive(player);
		
		data.put(isAlive ? (byte) 1 : (byte) 0);
		
		if(isAlive) {
			Tank tank = world.getTank(player);
			
			data.put((byte) tank.getX());
			data.put((byte) tank.getY());
			
			data.put((byte) world.getWidth());
			data.put((byte) world.getHeight());
			
			world.writeWorld(data);
			
			Collection<Tank> visibleTanks = world.getVisibleTanks(player);
			for(Tank otherTank : visibleTanks) {
				data.put((byte) otherTank.getX());
				data.put((byte) otherTank.getY());
			}
		}
		
		return true;
	}

	@Override
	public synchronized void processData(ByteBuffer data, Player player) {
		if(!world.isAlive(player))
			return;
		
		//0: do nothing
		//1: move
		//2: shoot
		Action action = Action.values()[data.get()];
		switch(action) {
		case MOVE:
			world.moveTank(player, Direction.values()[data.get()]);
			break;
		case NOTHING:
			break;
		case SHOOT:
			Tank tank = world.getTank(player);
			tank.serDirection(Direction.values()[data.get()]);
			world.tanksToFire.add(world.getTank(player));
			break;
		}
	}

	@Override
	public String getName() {
		return "Tank game";
	}

	@Override
	public String getDescription() {
		return "A tatical 2D tank game with stealth mechanics.";
	}

	@Override
	public synchronized void displayGame(DisplayHandler handler) {
		world.displayWorld(handler);
		handler.print();
	}

	@Override
	public void stop() {}

	@Override
	public void setHandler(GameHandler gameHandler) {
		handler = gameHandler;
	}

	@Override
	public synchronized void addPlayer(Player player) {
		world.spawnTank(player);
	}
}
