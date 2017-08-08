package brownshome.scriptwars.game.tanks;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.*;
import brownshome.scriptwars.server.Server;

public class World {
	private List<Shot> shots = new LinkedList<>();
	private Map<Player<?>, Tank> tanks = new HashMap<>();
	private Set<Tank> clientTanks = new HashSet<>();
	private boolean[][] map;
	
	private Tank[][] tankMap;
	private Shot[][] shotMap;
	private Set<Coordinates> ammoPickup = new HashSet<>();
	
	private Set<Tank> tanksToFire = new HashSet<>();
	private Set<Player<?>> playersToSpawn = new HashSet<>();
	
	private Collection<Shot> deadShotsToRender = new ArrayList<>();
	Collection<Tank> deadTanksToRender = new ArrayList<>();
	
	private TankGame game;
	
	protected World(boolean[][] map, TankGame game) {
		assert map.length > 0 && map[0].length > 0;
		
		this.map = map;
		this.game = game;
		tankMap = new Tank[map.length][map[0].length];
		shotMap = new Shot[map.length][map[0].length];
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
		shotMap = new Shot[height][width];
		
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
			Shot shot = new Shot(network);
			shots.add(shot);
			addToShotMap(shot);
		}
		
		int ammoCount = network.getByte();
		for(int i = 0; i < ammoCount; i++) {
			Coordinates coord = new Coordinates(network);
			ammoPickup.add(coord);
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

	public Shot getShot(int x, int y) {
		return shotMap[y][x];
	}
	
	public Tank getTank(Coordinates c) {
		return getTank(c.getX(), c.getY());
	}

	public Shot getShot(Coordinates c) {
		return getShot(c.getX(), c.getY());
	}
	
	private void setTank(Coordinates c, Tank tank) {
		tankMap[c.getY()][c.getX()] = tank;
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

	protected void moveTank(Player<?> player, Direction direction) {
		if(!isAlive(player))
			throw new IllegalStateException("You are not alive, you cannot move.");
		
		Tank tank = tanks.get(player);
		tank.getPosition();
		
		tank.move(direction);
		setTank(tank.getPosition(), tank);
	}

	protected void fireNextTick(Player<?> player) {
		tanksToFire.add(getTank(player));
	}

	//Check if every tank is in the spot they think they are. For any tanks that
	//are overwrote roll them back as well as the tank overriding them. Then repeat.
	protected void finalizeMovement() {
		Set<Tank> tanksToRollBack = new HashSet<>();
		
		for(Tank tank : tanks.values()) {
			Tank other = getTank(tank.getPosition());
			if(other != tank) {
				if(other != null)
					tanksToRollBack.add(other);
				
				tanksToRollBack.add(tank);
			}
		}
		
		//clear tankMap
		tankMap = new Tank[getHeight()][getWidth()];		
		
		if(tanksToRollBack.isEmpty()) {
			//CHECK FOR TANKS SWAPPING POSITIONS
			//For each tank, if it moved. Check if there is a tank on it's old space
			//that just moved from it's space.
			
			for(Tank tank : tanks.values())
				setTank(tank.getPosition(), tank);
			
			for(Tank tank : tanks.values()) {
				if(tank.hasMoved()) {
					Coordinates space = tank.getPosition();
					Coordinates oldSpace = tank.getDirection().opposite().move(tank.getPosition());

					Tank otherTank = getTank(oldSpace);
					if(
							otherTank != null
							&& otherTank.hasMoved()
							&& otherTank.getDirection().opposite() == tank.getDirection()
					) {
						tanksToRollBack.add(tank);
						tanksToRollBack.add(otherTank);
					}
				}
			}
			
			if(tanksToRollBack.isEmpty()) {
				for(Tank t : tanks.values()) {
					t.clearHasMoved();
				}
				
				return;
			}
		}
		
		for(Tank tank : tanksToRollBack) {
			tank.rollBack();
		}
		
		for(Tank tank : tanks.values()) {
			setTank(tank.getPosition(), tank);
		}
		
		//at most we recurse MAX_PLAYERS times. No risk of stack overflow
		finalizeMovement();
	}
	
	protected void pickupAmmo() {
		for(Tank tank : tanks.values()) {
			if(ammoPickup.remove(tank.getPosition())) {
				tank.refilAmmo();
			}
		}
		
		try {
			while(ammoPickup.size() < game.numberOfAmmoPickups()) {
				ammoPickup.add(getAmmoSpawnCoordinate());
			}
		} catch(IllegalStateException e) { /*No valid spawn slots*/ }
	}

	private Coordinates getAmmoSpawnCoordinate() {
		List<Coordinates> possibleSpawns = new ArrayList<>(getWidth() * getHeight());
		
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				Coordinates coord = new Coordinates(x, y);
				if(!isWall(coord) && getTank(coord) == null) {
					possibleSpawns.add(coord);
				}
			}
		}
		
		if(possibleSpawns.isEmpty()) throw new IllegalStateException();
		
		return possibleSpawns.get(new Random().nextInt(possibleSpawns.size()));
	}

	protected void fireTanks() {
		for(Tank tank : tanksToFire) {
			fireTank(tank);
		}
		
		tanksToFire.clear();
		
		//Check to see if any shots are in the same spot, delete them
		Set<Shot> shotsToRemove = new HashSet<>();
		for(Shot shot : shots) {
			Shot other = getShot(shot.getPosition());
			if(other != shot) {
				shotsToRemove.add(other);
				shotsToRemove.add(shot);
				deadShotsToRender.add(other);
				deadShotsToRender.add(shot);
			}
		}
		
		shots.removeIf(s -> {
			if(shotsToRemove.contains(s)) {
				removeShotFromMap(s);
				return true;
			}
			
			return false;
		});
	}
	
	private void fireTank(Tank tank) {
		Direction direction = tank.getDirection();
		Coordinates bulletSpawn = direction.move(tank.getPosition());
		
		if(!tank.removeAmmo())
			return;
		
		Shot shot = new Shot(bulletSpawn, tank, this, direction);
		
		if(isWall(bulletSpawn)) {
			deadShotsToRender.add(shot);
			return;
		}
		
		Tank otherTank = getTank(bulletSpawn);
		if(otherTank != null) {
			tank.getOwner().addScore(1);
			tank.refilAmmo();
			otherTank.kill();
			deadShotsToRender.add(shot);
			return;
		}
		
		shots.add(shot);
		addToShotMap(shot);
	}

	private void addToShotMap(Shot shot) {
		shotMap[shot.getPosition().getY()][shot.getPosition().getX()] = shot;
	}
	
	protected void moveShots() {
		shots.forEach(Shot::updatePrevious);
		for(int i = 0; i < Shot.SPEED; i++) {
			moveShotsOnce();
		}
	}
	
	private void moveShotsOnce() {
		//Delete shots that are swapping
		//Delete shots that will collide
		//Move shots, taking into account walls and such things
		
		List<Shot> deadShots = new ArrayList<>(shots.size());
		
		//Check swapping
		shots.removeIf(s -> {
			Shot other;
			Coordinates nextPos = s.getDirection().move(s.getPosition());
			
			//There is a shot in nextPos and it is coming towards us.
			if((other = getShot(nextPos)) != null && other.getDirection().opposite() == s.getDirection()) {
				deadShots.add(s);
				return true;
			}
			
			return false;
		});
		
		for(Shot s : deadShots) {
			removeShotFromMap(s);
			//TODO have the shots collide at the half extents
		}
		
		deadShots.clear();
		
		//Check for collisions
		shots.removeIf(s -> {
			Coordinates nextPos = s.getDirection().move(s.getPosition());
			if(isWall(nextPos)) {
				deadShots.add(s);
				return true;
			}
			
			//Check every direction into the new space other than the one we came from
			for(Direction dir = s.getDirection().opposite().clockwise(); dir != s.getDirection().opposite(); dir = dir.clockwise()) {
				Shot other = getShot(dir.move(nextPos));
				if(other != null && other.getDirection().opposite() == dir) {
					deadShots.add(s);
					return true;
				}
			}
			
			return false;
		});
		
		for(Shot s : deadShots) {
			removeShotFromMap(s);
			s.tickShot(); //Make the position of the shot correct
			deadShotsToRender.add(s);
		}
		
		deadShots.clear();
		
		shots.removeIf(s -> {
			removeShotFromMap(s);
			
			if(s.tickShot()) {
				deadShots.add(s);
				deadShotsToRender.add(s);
				return true;
			} else {
				addToShotMap(s);
			}
			
			return false;
		});
		
		for(Shot s : deadShots) {
			s.completeTick();
		}
	}

	private void removeShotFromMap(Shot shot) {
		if(shotMap[shot.getPosition().getY()][shot.getPosition().getX()] == shot)
			shotMap[shot.getPosition().getY()][shot.getPosition().getX()] = null;
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
			setTank(coord, tank);
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
		
		for(Shot shot : shots) {
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
	
	private boolean canSee(Coordinates a, Coordinates b) {
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
	
	private class TankGridItem implements GridItem {
		private final Coordinates start, end;
		private final byte code;
		
		public TankGridItem(Tank tank) {
			if(tank.shouldRenderMove()) { //Direction is used to free up hasMoved
				Direction dir = tank.getDirection().opposite();
				end = tank.getPosition();
				start = dir.move(end);
				tank.clearRenderMoved();
			} else {
				start = end = tank.getPosition();
			}
			
			code = (byte) (game.getIndex(tank.getOwner()) + TankGameDisplayHandler.DynamicSprites.TANK_START.ordinal());
		}
		
		@Override public byte code() { return code; }
		@Override public Coordinates start() { return start; }
		@Override public Coordinates end() { return end; }
	}
	
	public static class ShotGridItem implements GridItem {
		private final Coordinates start, end;
		
		public ShotGridItem(Shot shot) {
			start = shot.getPrevious();
			end = shot.getPosition();
		}

		@Override public byte code() { return (byte) TankGameDisplayHandler.DynamicSprites.SHOT.ordinal(); }
		@Override public Coordinates start() { return start; }
		@Override public Coordinates end() { return end; }
	}
	
	public static class AmmoPickupGridItem implements GridItem {
		private final Coordinates position;
		
		public AmmoPickupGridItem(Coordinates position) {
			this.position = position;
		}

		@Override public byte code() { return (byte) TankGameDisplayHandler.DynamicSprites.AMMO.ordinal(); }
		@Override public Coordinates start() { return position; }
		@Override public Coordinates end() { return position; }
	}
	
	public void displayWorld(TankGameDisplayHandler handler) {
		//TODO add dead shots and tanks
		
		Collection<GridItem> items = new ArrayList<>();
		for(Tank tank : tanks.values()) {
			items.add(new TankGridItem(tank));
		}
		
		for(Shot shot : shots) {
			items.add(new ShotGridItem(shot));
		}
		
		for(Shot shot : deadShotsToRender) {
			items.add(new ShotGridItem(shot)); //This includes shots that never existed
		}
		
		for(Tank tank : deadTanksToRender) {
			items.add(new TankGridItem(tank));
		}
		
		for(Coordinates pickup : ammoPickup) {
			items.add(new AmmoPickupGridItem(pickup));
		}
		
		deadTanksToRender.clear();
		deadShotsToRender.clear();
		
		handler.setDynamicItems(items);
	}
	
	protected void removeTank(Tank tank) {
		removeTank(tank.getOwner());
		
	}

	protected Tank getTank(Player<?> player) {
		return tanks.get(player);
	}

	protected int getDataSize() {
		return (getWidth() * getHeight() - 1) / Byte.SIZE + 1;
	}

	protected void removeTank(Player<?> player) {
		Tank tank = tanks.remove(player);
		setTank(tank.getPosition(), null);
		
		shots.removeIf(s -> {
			if(s.getOwner() == tank) {
				removeShotFromMap(s);
				return true;
			}
			
			return false;
		});
	}

	/**
	 * Only use on the client side, do not edit.
	 * @return The tanks.
	 */
	public Collection<Tank> getTanks() {
		return clientTanks;
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
}
