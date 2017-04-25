package brownshome.scriptwars.server.game.tanks;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import javax.imageio.ImageIO;

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

public class TankGame extends Game<GridDisplayHandler> {
	static final int PLAYER_COUNT = 8;
	volatile boolean updatePlayerLists;
	final List<Player> players = Arrays.asList(new Player[PLAYER_COUNT]);
	World world;
	
	
	public TankGame(boolean[][] map, GameType type) throws OutOfIDsException {
		super(type, new GridDisplayHandler());
		getDisplayHandler().setPlayerList(players);
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
	protected void onPlayerChange() {
		super.onPlayerChange();
		updatePlayerLists = true;
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
	public void displayGame(GridDisplayHandler handler) {
		world.displayWorld(handler);
		
		if(updatePlayerLists) {
			updatePlayerLists = false;
			handler.sendPlayerIDs();	
		}
		
		handler.print();
	}

	@Override
	public void addPlayer(Player player) {
		task: {
			for(int i = 0; i < PLAYER_COUNT; i++) {
				if(players.get(i) == null) {
					players.set(i, player);
					break task;
				}
			}
			
			throw new RuntimeException("Too many players");
		}
		
		super.addPlayer(player);
		world.spawnTank(player);
	}

	public int getIndex(Player owner) {
		for(int i = 0; i < players.size(); i++) {
			if(players.get(i) == owner)
				return i;
		}
		
		throw new RuntimeException("Player not found");
	}
	
	@Override
	public void removePlayer(Player player) {
		task: {
			for(int i = 0; i < players.size(); i++) {
				if(players.get(i) == player) {
					players.set(i, null);
					break task;
				}
			}
			
			throw new RuntimeException("That player does not exist");
		}
		
		super.removePlayer(player);
		if(world.isAlive(player))
			world.removeTank(player);
	}

	@Override
	public int getPreferedConnectionType() {
		return UDPConnectionHandler.UPD_PROTOCOL_BYTE;
	}

	@Override
	public BufferedImage getIcon(Player player, Function<String, File> pathTranslator) throws IOException {
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
}
