package brownshome.scriptwars.server.game.tanks;

import java.nio.ByteBuffer;
import java.util.Collection;

import brownshome.scriptwars.server.connection.UDPConnectionHandler;
import brownshome.scriptwars.server.game.DisplayHandler;
import brownshome.scriptwars.server.game.Game;
import brownshome.scriptwars.server.game.GameHandler;
import brownshome.scriptwars.server.game.OutOfIDsException;
import brownshome.scriptwars.server.game.Player;

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

public class TankGame extends Game {
	GameHandler handler;
	World world;
	
	public TankGame(boolean[][] map) throws OutOfIDsException {
		super(new UDPConnectionHandler(), new DisplayHandler());
		
		this.world = new World(map, this);
	}
	
	public TankGame() throws OutOfIDsException {
		this(generateMap(5));
	}
	
	private static boolean[][] generateMap(int size) {
		boolean[][] map = new boolean[size][size];
		
		for(int i = 0; i < size; i++) {
			map[0][i] = true;
			map[i][0] = true;
			map[size - 1][i] = true;
			map[i][size - 1] = true;
		}
		
		return map;
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

	public static String getName() {
		return "Tanks";
	}

	public static String getDescription() {
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
	public synchronized void addPlayer(Player player) {
		world.spawnTank(player);
	}
}
