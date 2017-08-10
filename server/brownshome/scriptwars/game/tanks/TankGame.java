package brownshome.scriptwars.game.tanks;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import javax.imageio.ImageIO;

import brownshome.scriptwars.connection.ConnectionHandler;
import brownshome.scriptwars.connection.InvalidIDException;
import brownshome.scriptwars.connection.UDPConnectionHandler;
import brownshome.scriptwars.game.*;
import brownshome.scriptwars.game.tanks.ai.*;

/* Each tick shots are moved x spaces. Then tanks shoot. Then tanks move */

public class TankGame extends Game {
	public static final int PLAYER_COUNT = 8;
	private static final int NUMBER_OF_AMMO_PICKUPS = 5;
	
	private final List<Player<?>> players = Arrays.asList(new Player[PLAYER_COUNT]);
	private final Map<Player<?>, TankStats> stats = new HashMap<>();
	private World world;
	
	public TankGame(boolean[][] map, GameType type) throws OutOfIDsException {
		super(type);
		this.world = new World(map, this);
		this.getDisplayHandler().putStaticGrid(getStaticGrid());
	}
	
	protected TankStats getStatsObject(Player<?> player) {
		return stats.get(player);
	}
	
	@Override
	protected DisplayHandler constructDisplayHandler() {
		return new TankGameDisplayHandler(this);
	}
	
	@Override
	public TankGameDisplayHandler getDisplayHandler() {
		return (TankGameDisplayHandler) super.getDisplayHandler();
	}
	
	public TankGame(GameType type) throws OutOfIDsException {
		this(MapGenerator.getGenerator().withSize(25, 25).generate(), type);
	}
	
	public TankGame(GameType type, int ticks, int timeout) throws OutOfIDsException {
		this(MapGenerator.getGenerator().withSize(25, 25).generate(), type, ticks, timeout);
	}
	
	public TankGame(boolean[][] map, GameType type, int ticks, int timeout) throws OutOfIDsException {
		super(type, ticks, timeout);
		this.world = new World(map, this);
	}

	@Override
	public boolean hasPerPlayerData() {
		return true;
	}

	@Override
	public int getMaximumPlayers() {
		return PLAYER_COUNT;
	}

	@Override
	public int getTickRate() {
		return 250;
	}

	@Override
	public void tick() {
		world.tick();
	}

	@Override
	public int getDataSize() {
		return 
				9                                             //Headers and single values
				+ getMaximumPlayers() * 3                     //Tank x, y, id
				+ world.getDataSize()                         //World data
				+ Tank.MAX_AMMO * getMaximumPlayers() * 3     //Shot data x, y, direction
				+ numberOfAmmoPickups() * 2;                  //Ammo pickups x, y
	}

	protected int numberOfAmmoPickups() {
		return NUMBER_OF_AMMO_PICKUPS;
	}

	/**
	 * 
	 * byte: 0/1 alive or dead
	 * byte: ammoRemaining
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
	 *	byte: id
	 * }
	 * 
	 * byte: shots
	 * shots * {
	 * 	byte: x
	 * 	byte: y
	 * 	byte: direction
	 * }
	 * 
	 * byte: ammoPickups
	 * shots * {
	 *  byte: x
	 *  byte: y
	 * }
	 */
	@Override
	public boolean getData(Player<?> player, ByteBuffer data) {
		boolean isAlive = world.isAlive(player);
		
		data.put(isAlive ? (byte) 1 : (byte) 0);
		
		if(isAlive) {
			Tank tank = world.getTank(player);
			
			data.put((byte) tank.ammo());
			
			data.put((byte) tank.getPosition().getX());
			data.put((byte) tank.getPosition().getY());
			
			data.put((byte) world.getWidth());
			data.put((byte) world.getHeight());
			
			world.writeWorld(data);
			
			Collection<Tank> visibleTanks = world.getVisibleTanks(player);
			data.put((byte) visibleTanks.size());
			for(Tank otherTank : visibleTanks) {
				int slot = getIndex(otherTank.getOwner());
				data.put((byte) otherTank.getPosition().getX());
				data.put((byte) otherTank.getPosition().getY());
				data.put((byte) slot);
			}
			
			Collection<Shot> shots = world.getShots();
			data.put((byte) shots.size());
			for(Shot shot : shots) {
				data.put((byte) shot.getPosition().getX());
				data.put((byte) shot.getPosition().getY());
				data.put((byte) shot.getDirection().ordinal());
			}
			
			Collection<Coordinates> ammoPickups = world.getAmmoPickups();
			data.put((byte) ammoPickups.size());
			for(Coordinates pickup : ammoPickups) {
				data.put((byte) pickup.getX()).put((byte) pickup.getY());
			}
		}
		
		return true;
	}

	@Override
	public void processData(ByteBuffer data, Player<?> player) {
		if(!world.isAlive(player))
			world.spawnTank(player);
		
		Action action = Action.values()[data.get()];
		switch(action) {
		case MOVE:
		case SHOOT:
			if(!world.isAlive(player))
				throw new IllegalArgumentException("It is not legal to shoot or move while you are dead.");
			
			Tank tank = world.getTank(player);
			tank.setNextAction(Direction.values()[data.get()], action);
			break;
		case NOTHING:
			break;
		}
	}

	public static String getName() {
		return "Tanks";
	}

	public static String getDescription() {
		return "A tactical 2D tank game with stealth mechanics.";
	}

	public static Map<String, BotFunction> getBotFunctions() {
		Map<String, BotFunction> mapping = new HashMap<>();
		//mapping.put("Hard AI", HardAI::main);
		mapping.put("Random AI", RandomAI::main);
		mapping.put("Simple AI", SimpleAI::main);
		//mapping.put("Scared AI", ScaredAI::main);
		//mapping.put("Aggressive AI", AggressiveAI::main);
		
		return mapping;
	}
	
	@Override
	public void displayGame() {
		TankGameDisplayHandler handler = getDisplayHandler();
		
		world.displayWorld(handler);
		handler.sendUpdates();
	}

	@Override
	public void addPlayer(Player<?> player) throws InvalidIDException {
		super.addPlayer(player);
		
		task: {
			for(int i = 0; i < PLAYER_COUNT; i++) {
				if(players.get(i) == null) {
					players.set(i, player);
					break task;
				}
			}
			
			assert false : "Too many players";
		}
		
		stats.put(player, new TankStats(player));
		world.spawnTank(player);
	}

	public int getIndex(Player<?> owner) {
		int result = players.indexOf(owner);
		
		if(result == -1)
			throw new RuntimeException("Player not found");
		
		return result;
	}
	
	@Override
	public void removePlayer(Player<?> player) {
		super.removePlayer(player);
		
		task: {
			for(int i = 0; i < players.size(); i++) {
				if(players.get(i) == player) {
					players.set(i, null);
					break task;
				}
			}
			
			assert false : "That player does not exist";
		}
		
		if(world.isAlive(player))
			world.removeTank(world.getTank(player));
	}

	@Override
	public ConnectionHandler<?> getPreferedConnectionHandler() {
		return UDPConnectionHandler.instance();
	}

	@Override
	public BufferedImage getIcon(Player<?> player, Function<String, File> pathTranslator) throws IOException {
		Color colour = Player.colours[this.getIndex(player)];
		
		BufferedImage result = ImageIO.read(pathTranslator.apply("icon.png"));
		for(int x = 0; x < result.getWidth(); x++) {
			for(int y = 0; y < result.getHeight(); y++) {
				Color original = new Color(result.getRGB(x, y), true);
				int r =  blend(original.getRed(), colour.getRed());
				int g = blend(original.getGreen(), colour.getGreen());
				int b = blend(original.getBlue(), colour.getBlue());
				result.setRGB(x, y, original.getAlpha() << 24 | r << 16 | g << 8 | b);
			}
		}
		
		return result;
	}
	
	private int blend(int a, int b) {
		return 255 - (255 - a) * (255 - b) / 255;
	}
	
	byte[][] getStaticGrid() {
		byte[][] grid = new byte[world.getHeight()][world.getWidth()];
		
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				if(world.isWall(x, y))
					grid[y][x] = (byte) TankGameDisplayHandler.StaticSprites.WALL.ordinal();
				else 
					grid[y][x] = (byte) TankGameDisplayHandler.StaticSprites.NOTHING.ordinal();
			}
		}
		
		return grid;
	}

	@Override
	public Map<String, Integer> getStats(Player<?> player) {
		Map<String, Integer> statsMapping = new HashMap<>();
		TankStats statsObject = stats.get(player);
		
		statsMapping.put("Ammo Picked Up", statsObject.getAmmoPickedUp());
		statsMapping.put("Deaths", statsObject.getDeaths());
		statsMapping.put("Kills", statsObject.getKills());
		statsMapping.put("Moves Attempted", statsObject.getMovesMade());
		statsMapping.put("Shots Fired", statsObject.getShotsFired());
		statsMapping.put("Moves Failed", statsObject.getMovesFailed());
		
		return statsMapping;
	}
}
