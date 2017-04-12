package brownshome.scriptwars.server.game.tanks;

import java.nio.ByteBuffer;
import java.util.Collection;

import brownshome.scriptwars.server.connection.*;
import brownshome.scriptwars.server.game.*;

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
	World world;
	
	public TankGame(boolean[][] map, GameType type) throws OutOfIDsException {
		super(type);
		
		this.world = new World(map, this);
	}
	
	public TankGame(GameType type) throws OutOfIDsException {
		this(
				MapGenerator.getGenerator()
					.withSize(20, 20)
					.generate(),
				type
			);
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
		return 250;
	}

	@Override
	public void tick() {
		world.rollBackTanks();
		world.moveShots();
		world.fireTanks();
	}

	@Override
	public int getDataSize() {
		//bytes + worldsize + bulletData
		return 7 + getMaximumPlayers() * 2 + world.getDataSize() + Tank.MAX_AMMO * getMaximumPlayers() * 3;
	}

	/**
	 * 
	 * byte: 0/1 alive or dead
	 * byte: x
	 * byte: y
	 * byte: width
	 * byte: height
	 * 
	 * width * height * boolean: wall array
	 * 
	 * byte: players
	 * players * {
	 * 	byte: x
	 * 	byte: y
	 * }
	 * 
	 * byte: shots
	 * shots * {
	 * 	byte: x
	 * 	byte: y
	 * 	byte: direction
	 * }
	 */
	@Override
	public boolean getData(Player player, ByteBuffer data) {
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
			data.put((byte) visibleTanks.size());
			for(Tank otherTank : visibleTanks) {
				data.put((byte) otherTank.getX());
				data.put((byte) otherTank.getY());
			}
			
			Collection<Shot> shots = world.getShots();
			data.put((byte) shots.size());
			for(Shot shot : shots) {
				data.put((byte) shot.getX());
				data.put((byte) shot.getY());
				data.put((byte) shot.getDirection().ordinal());
			}
		}
		
		return true;
	}

	@Override
	public void processData(ByteBuffer data, Player player) {
		if(!world.isAlive(player))
			world.spawnTank(player);
		
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
			tank.setDirection(Direction.values()[data.get()]);
			world.tanksToFire.add(world.getTank(player));
			break;
		}
	}

	public static String getName() {
		return "Tanks";
	}

	public static String getDescription() {
		return "A tactical 2D tank game with stealth mechanics.";
	}

	@Override
	public void displayGame(DisplayHandler handler) {
		world.displayWorld(handler);
		handler.print();
	}

	@Override
	public void addPlayer(Player player) {
		super.addPlayer(player);
		world.spawnTank(player);
	}

	@Override
	public void removePlayer(Player player) {
		super.removePlayer(player);
		if(world.isAlive(player))
			world.removeTank(player);
	}

	@Override
	public int getPreferedConnectionType() {
		return UDPConnectionHandler.UPD_PROTOCOL_BYTE;
	}
}
