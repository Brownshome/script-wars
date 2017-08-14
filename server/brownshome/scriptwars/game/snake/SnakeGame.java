package brownshome.scriptwars.game.snake;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.liamtbrand.snake.controller.AbstractGameObject;
import com.liamtbrand.snake.controller.AbstractSnake;
import com.liamtbrand.snake.engine.Engine;
import com.liamtbrand.snake.engine.mechanic.AbstractMechanic;
import com.liamtbrand.snake.engine.mechanic.SpawnFoodMechanic;
import com.liamtbrand.snake.model.IMapModel;
import com.liamtbrand.snake.model.ISnakeModel.Direction;
import com.liamtbrand.snake.model.concrete.Stage;
import com.liamtbrand.snake.model.concrete.test.TestMap;

import brownshome.scriptwars.connection.ConnectionHandler;
import brownshome.scriptwars.connection.ConnectionUtil;
import brownshome.scriptwars.connection.InvalidIDException;
import brownshome.scriptwars.connection.UDPConnectionHandler;
import brownshome.scriptwars.game.BotFunction;
import brownshome.scriptwars.game.DisplayHandler;
import brownshome.scriptwars.game.Game;
import brownshome.scriptwars.game.GameType;
import brownshome.scriptwars.game.Player;
import brownshome.scriptwars.game.snake.World.WorldException;

public class SnakeGame extends Game {
	private final Engine engine;
	private World world;
	
	public final int BYTE_LENGTH = 8;
	
	//Used on the website
	public SnakeGame(GameType type) {
		super(type);
		world = new World();
		engine = new Engine(new Stage(new TestMap()));
		setupEngine();
		getDisplayHandler().putStaticGrid(getStaticGrid());
	}

	//Used by the judging server
	public SnakeGame(GameType type, int ticks, int timeout) {
		super(type, ticks, timeout);
		world = new World();
		engine = new Engine(new Stage(new TestMap()));
		setupEngine();
		getDisplayHandler().putStaticGrid(getStaticGrid());
	}
	
	// Generate static grid from map.
	byte[][] getStaticGrid() {
		// Grid is x y flipped.
		byte[][] grid = new byte[engine.stage.getMap().getHeight()][engine.stage.getMap().getWidth()];
		
		for(int x = 0; x < engine.stage.getMap().getWidth(); x++) {
			for(int y = 0; y < engine.stage.getMap().getHeight(); y++) {
				if(engine.stage.getMap().isWall(x, y))
					grid[y][x] = (byte) SnakeGameDisplayHandler.StaticSprites.WALL.ordinal();
				else 
					grid[y][x] = (byte) SnakeGameDisplayHandler.StaticSprites.NOTHING.ordinal();
			}
		}
		 
		return grid;
	}

	private void setupEngine() {
		engine.stage = new Stage(new TestMap());
		AbstractMechanic spawnFood = new SpawnFoodMechanic(engine);
	}
	
	@Override
	public boolean hasPerPlayerData() {
		return false;
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
	protected void tick() {
		engine.tick();
	}

	public static String getName() {
		return "Snakes";
	}

	public static String getDescription() {
		return "A 2D snake game, just like your Nokia used to play.";
	}
	
	public static Map<String, BotFunction> getBotFunctions() {
		//Add Server AI here
		return Collections.emptyMap();
	}
	
	@Override
	public BufferedImage getIcon(Player<?> player, Function<String, File> pathTranslator) throws IOException {
		return ImageIO.read(pathTranslator.apply("icon.png"));
		//You may want to apply some colouring here
	}
	
	private int getMapDataSize() {
		IMapModel map = engine.stage.getMap();
		int mapSize = 0;
		mapSize += 2; // width, height
		mapSize += (int) Math.ceil(map.getHeight()*map.getWidth()/8);
		return mapSize;
	}
	
	private int getSnakesDataSize() {
		int snakesSize = 0;
		snakesSize += 1; // number of snakes.
		Iterator<AbstractSnake> snakeIterator = engine.stage.getSnakeIterator();
		AbstractSnake snake;
		while(snakeIterator.hasNext()) {
			snake = snakeIterator.next();
			if(!snake.destroyed()) {
				snakesSize += Integer.BYTES + Byte.BYTES; // Snake id, length.
				snakesSize += snake.model.getLength() * 2; // length: segmentx, segmenty
			}
		}
		return snakesSize;
	}
	
	private int getGameObjectsDataSize() {
		int objectsSize = 0;
		objectsSize += 1; // number of objects
		Iterator<AbstractGameObject> goIterator = engine.stage.getGameObjectIterator();
		AbstractGameObject object;
		while(goIterator.hasNext()) {
			object = goIterator.next();
			if(!object.destroyed()) {
				objectsSize += 3; // x, y, type
			}
		}
		return objectsSize;
	}

	@Override
	public int getDataSize() {
		
		//TODO Build the data protocol with the clients
		
		/*
		 * Things to send:
		 * - Map data
		 *   - width,height
		 *   - boolean array representing walls
		 * - For each snake
		 *   - id,length
		 *   - Segments      <- for all segments of the snake (starting from head).
		 *     - x,y
		 *     
		 * - Objects     <- for all objects
		 *   - x,y,type
		 */
		
		return
				  getMapDataSize()
				+ getSnakesDataSize()
				+ getGameObjectsDataSize()
		;
	}

	@Override
	public boolean getData(Player<?> player, ByteBuffer data) {
		
		//TODO Build the data protocol with the clients
		
		// Build the data for the map.
		IMapModel map = engine.stage.getMap();
		
		ByteBuffer buff;
		
		buff = ByteBuffer.wrap(new byte[getMapDataSize()]);
		
		// First two bytes give the size of the map.
		// Casting to bytes. Max map size on networked games is 256 by 256.
		if((int) ((byte) map.getWidth()) != map.getWidth()) {
			// Signal some problem!
		}
		buff.put((byte) map.getWidth());
		buff.put((byte) map.getHeight());
		
		ConnectionUtil.BooleanWriter writer = new ConnectionUtil.BooleanWriter(buff);
		// Map data, bits, packed into bytes.
		for(int y = 0; y < map.getHeight(); y++) {
			for(int x = 0; x < map.getWidth(); x++) {
				writer.writeBoolean(map.isWall(x, y));
			}
		}
		
		writer.flush();
		
		buff.flip();
		data.put(buff);
		
		// Build the data for the players.
		Iterator<AbstractSnake> snakeIterator = engine.stage.getSnakeIterator();
		buff = ByteBuffer.wrap(new byte[getSnakesDataSize()]);
		int snakes = 0;
		AbstractSnake snake;
		Player<?> owner;
		while(snakeIterator.hasNext()) {
			snake = snakeIterator.next();
			if(!snake.destroyed()) {
				try {
					owner = world.getOwner(snake);
					buff.putInt(owner.getID()); // id
					buff.put((byte) snake.model.getLength()); // length
					for(int i = 0; i < snake.model.getLength(); i++) { // segments
						buff.put((byte) snake.model.getSegmentX(i)); 
						buff.put((byte) snake.model.getSegmentY(i));
					}
					
					snakes++;
				} catch (WorldException e) {
					// Problem getting snake, let's just ignore it for now...
				}
			}
		}
		
		data.put((byte) snakes);
		buff.flip();
		data.put(buff); // Add the snake data to the buffer.
		
		// Build the data for the game objects.
		Iterator<AbstractGameObject> goIterator = engine.stage.getGameObjectIterator();
		buff = ByteBuffer.wrap(new byte[getGameObjectsDataSize()]); // TODO optimize this.
		byte objects = (int) 0;
		AbstractGameObject object;
		while(goIterator.hasNext()) {
			object = goIterator.next();
			if(!object.destroyed()) {
				buff.put((byte) object.model.getX());
				buff.put((byte) object.model.getY());
				buff.put((byte) object.model.getType().ordinal());
				objects++;
			}
		}
		data.put(objects);
		buff.flip();
		data.put(buff); // Add the object data to the buffer.
		
		return true;
	}
	
	@Override
	public void addPlayer(Player<?> player) throws InvalidIDException {
		world.spawnSnake(player, engine);
		
		super.addPlayer(player);
	}

	@Override
	public void processData(ByteBuffer data, Player<?> player) {
		//assert false;
		
		if(!world.hasSnake(player)) {
			world.spawnSnake(player, engine);
		}
		
		// Incoming data from player:
		// Player's move: direction.
		// .ordinal() on Direction.
		if(data.hasRemaining()) {
		
			Direction direction = Direction.values()[data.get()];
			try {
				world.getSnake(player).model.setDirection(direction);
			} catch (WorldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public SnakeGameDisplayHandler getDisplayHandler() {
		return (SnakeGameDisplayHandler) super.getDisplayHandler();
	}

	@Override
	protected void displayGame() {
		SnakeGameDisplayHandler dh = getDisplayHandler();
		
		world.displayWorld(dh,engine);
		dh.sendUpdates();
	}

	@Override
	public ConnectionHandler<?> getPreferedConnectionHandler() {
		return UDPConnectionHandler.instance();
	}

	@Override
	protected DisplayHandler constructDisplayHandler() {
		return new SnakeGameDisplayHandler(this);
	}
}
