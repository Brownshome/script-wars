package brownshome.scriptwars.game.tanks;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.*;
import brownshome.scriptwars.server.Server;

public class World {
	private Map<Player<?>, Tank> tanks = new HashMap<>();
	
	private boolean[][] map;
	
	private Map<Coordinates, Shot> shotMap = new HashMap<>();
	private Map<Coordinates, Tank> tankMap = new HashMap<>();
	private Set<Coordinates> ammoPickup = new HashSet<>();
	
	private Set<Player<?>> playersToSpawn = new HashSet<>();
	private Collection<GridItem> deadGridItems = new ArrayList<>();
	private TankGame game;
	
	protected World(boolean[][] map, TankGame game) {
		assert map.length > 0 && map[0].length > 0;
		
		this.map = map;
		this.game = game;
	}

	/**
	 * Constructs a world using data from the given network
	 * @param network The network to read from
	 */
	public World(Network network) {
		int width = network.getByte();
		int height = network.getByte();
		
		map = new boolean[height][width];
		
		for(boolean[] row : map) {
			for(int x = 0; x < width; x++) {
				row[x] = network.getBoolean();
			}
		}
		
		int noTanks = network.getByte();
		for(int t = 0; t < noTanks; t++) {
			Tank tank = new Tank(network);
			addTankToMap(tank);
		}
		
		int shotCount = network.getByte();
		for(int s = 0; s < shotCount; s++) {
			Shot shot = new Shot(network);
			addShotToMap(shot);
		}
		
		int ammoCount = network.getByte();
		for(int i = 0; i < ammoCount; i++) {
			Coordinates coord = new Coordinates(network);
			ammoPickup.add(coord);
		}
	}
	
	protected void tick() {
		//Move Tanks
		boolean keepTrying;
		do {
			keepTrying = false;
			
			tankMap.clear();

			for(Tank tank : tanks.values()) {
				addTankToMap(tank);
			}
			
			keepTrying = false;
			for(Tank tank : tanks.values()) {
				keepTrying |= tank.finalizeMove();
			}
		} while(keepTrying);
		
		tankMap.clear();

		for(Tank tank : tanks.values()) {
			addTankToMap(tank);
		}
		
		//Pickup ammo
		for(Tank tank : tanks.values()) {
			tank.doAmmoPickups();
		}
		
		//Spawn ammo
		try {
			while(ammoPickup.size() < game.numberOfAmmoPickups())
				ammoPickup.add(getAmmoSpawnCoordinate());
		} catch(IllegalStateException ise) { /* No more places to spawn ammo */ }
		
		//Move Shots
		//A decoupled array to avoid conmodexceptions
		if(!shotMap.isEmpty()) {
			Shot[] coldShots = shotMap.values().toArray(new Shot[0]);
			for(Shot s : coldShots) s.updatePrevious();

			for(int i = 0; i < Shot.SPEED; i++) {
				shotMap.clear();
				for(Shot s : coldShots) s.tickShot();
				coldShots = shotMap.values().toArray(new Shot[0]); //Don't move shots that hit a wall
				
				for(Shot s : coldShots) s.detectCollisions();
				for(Shot s : coldShots) s.completeTick();
				coldShots = shotMap.values().toArray(new Shot[0]);
			}
		}
		
		//Fire Tanks
		Tank[] coldTanks = tanks.values().toArray(new Tank[0]);
		for(Tank t : coldTanks) t.finalizeShot();
		for(Tank t : coldTanks) t.removeIfDead();
		
		//Spawn players
		spawnPlayers();
		
		for(Tank t : tanks.values())
			t.clearAction();
	}

	protected void addDeadGridItem(GridItem item) {
		deadGridItems.add(item);
	}
	
	public Tank getTank(Coordinates c) {
		return tankMap.get(c);
	}

	public Shot getShot(Coordinates c) {
		return shotMap.get(c);
	}
	
	public boolean isWall(Coordinates c) {
		return isWall(c.getX(), c.getY());
	}

	public boolean isWall(int x, int y) {
		return map[y][x];
	}

	public Collection<Shot> getShots() {
		return shotMap.values();
	}

	public int getWidth() {
		return map[0].length;
	}

	public int getHeight() {
		return map.length;
	}
	
	private Coordinates getAmmoSpawnCoordinate() {
		List<Coordinates> possibleSpawns = new ArrayList<>(getWidth() * getHeight());
		
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				Coordinates coord = new Coordinates(x, y);
				if(!isWall(coord) && getTank(coord) == null && !ammoPickup.contains(coord)) {
					possibleSpawns.add(coord);
				}
			}
		}
		
		if(possibleSpawns.isEmpty()) throw new IllegalStateException();
		
		return possibleSpawns.get(new Random().nextInt(possibleSpawns.size()));
	}
	
	protected void removeShotFromMap(Shot shot) {
		shotMap.remove(shot.getPosition());
	}

	protected boolean isAlive(Player<?> player) {
		return tanks.containsKey(player);
	}

	protected Collection<Tank> getVisibleTanks(Player<?> player) {
		Tank tank = getTank(player);
		
		return tanks.values().stream()
				.filter(otherTank -> otherTank != tank)
				.filter(otherTank -> canSee(tank.getPosition(), otherTank.getPosition()))
				.collect(Collectors.toList());
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

	protected void spawnTank(Player<?> player) {
		playersToSpawn.add(player);
	}
	
	protected void spawnPlayers() {
		Player<?> player;
		for(Iterator<Player<?>> iterator = playersToSpawn.iterator(); iterator.hasNext(); ) {
			player = iterator.next();
			Coordinates coord = getSpawningCoordinate();
			if(coord == null) {
				Server.LOG.log(Level.INFO, "Unable to spawn all players");
				continue;
			}
				
			iterator.remove();
			
			Tank tank = new Tank(coord, player, this);
			tanks.put(player, tank);
			addTankToMap(tank);
		}
	}

	private Coordinates getSpawningCoordinate() {
		//spawn tanks in the farthest position away from players, weighting more towards open spaces.
		
		Coordinates bestCoordinate = null;
		float bestRank = Float.NEGATIVE_INFINITY;
		
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				Coordinates coord = new Coordinates(x, y);
				
				if(!isWall(coord) && getTank(coord) == null) {
					float rank = rankCoordinate(coord);
					if(bestRank < rank) {
						bestCoordinate = coord;
						bestRank = rank;
					}
				}
			}
		}
		
		return bestCoordinate;
	}
	
	private float rankCoordinate(Coordinates coord) {
		final float UNSAFE_RANK = -2f; //The rank lost for every turn under 10 that the space might be safe for.
		final float IS_CORNER = .5f; //The rank gained for being a corner
		final float IS_SEEN = 2f; //The rank gained for not being seen
		final float DISTANCE = .125f; //The rank gained for being far away from the nearest tank
		
		float rank = 0;
		
		for(Shot shot : getShots()) {
			Coordinates shotCoord = shot.getPosition();
			if(Direction.getDirection(coord, shotCoord) == shot.getDirection()) {
				int timeSurvived = 10;
				for(; !isWall(shotCoord); shotCoord = shot.getDirection().move(shotCoord)) {
					timeSurvived--;
					
					if(timeSurvived == 0) {
						break;
					}
					
					if(shotCoord.equals(coord)) {
						rank += UNSAFE_RANK * timeSurvived;
						break;
					}
				}
			}
		}
		
		for(Direction dir : Direction.values()) {
			if(!isWall(dir.move(coord)) && !isWall(dir.clockwise().move(coord))) {
				rank += IS_CORNER;
				break;
			}
		}
		
		boolean isSeen = false;
		int closestDistance = -1;
		for(Tank tank : tanks.values()) {
			if(!isSeen && canSee(tank.getPosition(), coord)) {
				rank -= IS_SEEN;
				isSeen = true;
			}
			
			int distance = distance(coord, tank.getPosition());
			if(closestDistance == -1 || distance < closestDistance) {
				closestDistance = distance;
			}
		}
		
		rank += DISTANCE * closestDistance;
		
		return rank;
	}
	
	private int distance(Coordinates a, Coordinates b) {
		return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
	}
	
	public boolean canSee(Coordinates a, Coordinates b) {
		int dx = 0;
		if(a.getX() < b.getX()) dx = 1;
		if(a.getX() > b.getX()) dx = -1;
		
		int dy = 0;
		if(a.getY() < b.getY()) dy = 1;
		if(a.getY() > b.getY()) dy = -1;
		
		if(dx == 0) {
			if(dy == 0) {
				return isWall(a);
			}
			
			for(int y = a.getY(); y != b.getY() + dy; y += dy) {
				if(isWall(a.getX(), y))
					return false;
			}
			
			return true;
		}
		
		if(dy == 0) {
			for(int x = a.getX(); x != b.getX() + dx; x += dx) {
				if(isWall(x, a.getY()))
					return false;
			}
			
			return true;
		}
		
		for(int x = a.getX(); x != b.getX() + dx; x += dx) {
			for(int y = a.getY(); y != b.getY() + dy; y += dy) {
				if(isWall(x, y))
					return false;
			}
		}
		
		return true;
	}
	
	private static class AmmoPickupGridItem implements GridItem {
		private final Coordinates position;
		
		public AmmoPickupGridItem(Coordinates position) {
			this.position = position;
		}

		@Override public byte code() { return (byte) TankGameDisplayHandler.DynamicSprites.AMMO.ordinal(); }
		@Override public Coordinates start() { return position; }
		@Override public Coordinates end() { return position; }
	}
	
	protected void displayWorld(TankGameDisplayHandler handler) {
		Collection<GridItem> items = new ArrayList<>();
		for(Tank tank : tanks.values()) {
			items.add(tank.getRenderItem());
		}
		
		for(Shot shot : getShots()) {
			items.add(shot.getRenderItem());
		}
		
		items.addAll(deadGridItems);
		
		for(Coordinates pickup : ammoPickup) {
			items.add(new AmmoPickupGridItem(pickup));
		}
		
		deadGridItems.clear();
		
		handler.setDynamicItems(items);
	}
	
	/** Removes a tank and all of it's shots from the game */
	protected void removeTank(Tank tank) {
		tanks.remove(tank.getOwner());
		tankMap.remove(tank.getPosition());
		getShots().removeIf(t -> t.getOwner() == tank);
	}

	protected Tank getTank(Player<?> player) {
		return tanks.get(player);
	}

	protected int getDataSize() {
		return (getWidth() * getHeight() - 1) / Byte.SIZE + 1;
	}

	boolean[][] getMap() {
		return map;
	}

	public Collection<Coordinates> getAmmoPickups() {
		return ammoPickup;
	}
	
	public boolean isAmmoPickup(Coordinates coord) {
		return ammoPickup.contains(coord);
	}

	protected boolean removeAmmoPickup(Coordinates position) {
		return ammoPickup.remove(position);
	}

	protected void addTankToMap(Tank tank) {
		tankMap.put(tank.getPosition(), tank);
	}
	
	protected void addShotToMap(Shot shot) {
		shotMap.put(shot.getPosition(), shot);
	}
	
	/** Returns the collection of tanks that the player can see. Do not edit. */
	public Collection<Tank> getTanks() {
		return tankMap.values();
	}
}
