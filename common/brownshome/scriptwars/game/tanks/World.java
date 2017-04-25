package brownshome.scriptwars.game.tanks;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import brownshome.scriptwars.client.*;
import brownshome.scriptwars.game.*;
import brownshome.scriptwars.game.tanks.Direction;

public class World {
	private List<Shot> shots = new ArrayList<>();
	private Map<Player, Tank> tanks = new HashMap<>();
	private Set<Tank> clientTanks = new HashSet<>();
	private boolean[][] map;
	private Tank[][] tankMap;
	private TankGame game;
	
	private Set<Tank> tanksToFire = new HashSet<>();
	private Set<Tank> tanksToRollBack = new HashSet<>();
	
	protected World(boolean[][] map, TankGame game) {
		assert map.length > 0 && map[0].length > 0;
		
		this.game = game;
		
		this.map = map;
		tankMap = new Tank[map.length][map[0].length];
	}

	/**
	 * Constructs a world using data from the given network
	 * @param network The network to read from
	 */
	public World(Network network) {
		int width = network.getByte();
		int height = network.getByte();
		
		map = new boolean[height][width];
		tankMap = new Tank[height][width];
		for(boolean[] row : map) {
			for(int x = 0; x < width; x++) {
				row[x] = network.getBoolean();
			}
		}
		
		int tanks = network.getByte();
		for(int t = 0; t < tanks; t++) {
			Tank tank = new Tank(network);
			tankMap[tank.getPosition().getY()][tank.getPosition().getX()] = tank;
			clientTanks.add(tank);
		}
		
		int shotCount = network.getByte();
		for(int s = 0; s < shotCount; s++) {
			shots.add(new Shot(network));
		}
	}

	/**
	 * @param x
	 * @param y
	 * @return null if there is no tank, returns the tank otherwise
	 */
	public Tank getTank(int x, int y) {
		return tankMap[y][x];
	}

	public Tank getTank(Coordinates c) {
		return getTank(c.getX(), c.getY());
	}

	public boolean isWall(Coordinates c) {
		return isWall(c.getX(), c.getY());
	}

	public boolean isWall(int x, int y) {
		return map[y][x];
	}

	public Collection<Shot> getShots() {
		return shots;
	}

	public int getWidth() {
		return map[0].length;
	}

	public int getHeight() {
		return map.length;
	}

	protected void moveTank(Player player, Direction direction) {
		Tank tank = tanks.get(player);
		tank.getPosition();
		
		tankMap[tank.getPosition().getY()][tank.getPosition().getX()] = null;
		tank.move(direction);
		tankMap[tank.getPosition().getY()][tank.getPosition().getX()] = tank;
	}

	protected void fireTank(Tank tank) {
		tanksToFire.remove(tank); //This prevents weird infinite loops
		
		Direction direction = tank.getDirection();
		
		Coordinates bulletSpawn = direction.move(tank.getPosition());
		
		if(isWall(bulletSpawn))
			return;
		
		Tank otherTank = getTank(bulletSpawn);
		if(otherTank != null) {
			if(tanksToFire.contains(otherTank)) {
				fireTank(otherTank);
			}
			
			//Needed to avoid strange circular gun chains
			if(isAlive(otherTank.getOwner()))
				otherTank.kill();
			
			return;
		}
		
		if(tank.removeAmmo()) {
			shots.add(new Shot(bulletSpawn, tank, this, direction));
		}
	}

	protected void fireNextTick(Player player) {
		tanksToFire.add(getTank(player));
	}

	protected void rollBackTanks() {
		for(Tank t : tanksToRollBack) {
			tankMap[t.getPosition().getY()][t.getPosition().getX()] = null;
			t.rollBack();
			tankMap[t.getPosition().getY()][t.getPosition().getX()] = t;
		}
		
		tanksToRollBack.clear();
		
		for(Tank t : tanks.values()) {
			t.clearHasMoved();
		}
	}

	protected void fireTanks() {
		while(!tanksToFire.isEmpty()) {
			fireTank(tanksToFire.iterator().next()); //not exactly efficient but it avoids concurrent mod exceptions
		}
	}

	protected void moveShots() {
		for(Iterator<Shot> it = shots.iterator(); it.hasNext(); ) {
			if(it.next().tickShot())
				it.remove();
		}
	}

	protected boolean isAlive(Player player) {
		return tanks.containsKey(player);
	}

	protected Collection<Tank> getVisibleTanks(Player player) {
		Collection<Tank> visibleTanks = new ArrayList<>();
		
		Tank tank = tanks.get(player);
		
		for(Direction direction : Direction.values()) {
			Coordinates coord = tank.getPosition();
			
			while(!isWall(coord = direction.move(coord))) {
				Tank spottedTank = getTank(coord);
				if(spottedTank != null) {
					visibleTanks.add(spottedTank);
				}
			}
		}
		
		return visibleTanks;
	}

	protected void writeWorld(ByteBuffer data) {
		byte buffer = 0;
		int index = 0;
		
		for(boolean[] row : map) {
			for(boolean isWall : row) {
					buffer |= (isWall ? 1 : 0) << index++;
					if(index == 8) {
						index = 0;
						data.put(buffer);
						buffer = 0;
					}
			}
		}
		
		if(index != 0) {
			data.put(buffer);
		}
	}

	protected void spawnTank(Player player) {
		Coordinates coord;
		
		//it's lazy but if there are enough tanks to make a difference then something crazy is happening anyway
		do {
			coord = Coordinates.getRandom(getWidth(), getHeight());
		} while(isWall(coord) || getTank(coord) != null);
		
		Tank tank = new Tank(coord, player, this);
		tanks.put(player, tank);
		tankMap[coord.getY()][coord.getX()] = tank;
	}

	protected void displayWorld(GridDisplayHandler handler) {
		char[][] display = new char[getHeight()][getWidth()];
		
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(isWall(x, y))
					display[y][x] = 1;
				else {
					Tank tank = getTank(x, y);
					if(tank != null) {
						display[y][x] = (char) (3 + game.getIndex(tank.getOwner())); //TODO fix this
					} else
						display[y][x] = 0;
				}
			}
		}
		
		for(Shot shot : shots) {
			display[shot.getPosition().getY()][shot.getPosition().getX()] = Shot.BULLET;
		}
		
		handler.putGrid(display);
		handler.print();
	}

	protected void removeTank(Tank tank) {
		removeTank(tank.getOwner());
	}

	protected void addToRewindList(Tank tank) {
		tanksToRollBack.add(tank);
	}

	protected Tank getTank(Player player) {
		return tanks.get(player);
	}

	protected int getDataSize() {
		return (getWidth() * getHeight() - 1) / Byte.SIZE + 1;
	}

	protected void removeTank(Player player) {
		Tank tank = tanks.remove(player);
		tankMap[tank.getPosition().getY()][tank.getPosition().getX()] = null;
	}

	/**
	 * Only use on the client side, do not edit.
	 * @return The tanks.
	 */
	public Collection<Tank> getTanks() {
		return clientTanks;
	}
}
