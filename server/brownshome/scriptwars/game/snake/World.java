package brownshome.scriptwars.game.snake;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.liamtbrand.snake.controller.AbstractGameObject;
import com.liamtbrand.snake.controller.AbstractSnake;
import com.liamtbrand.snake.controller.concrete.Snake;
import com.liamtbrand.snake.engine.Engine;
import com.liamtbrand.snake.model.ISnakeModel;
import com.liamtbrand.snake.model.ISnakeModel.Direction;
import com.liamtbrand.snake.model.concrete.BasicSnakeModel;

import brownshome.scriptwars.game.GridItem;
import brownshome.scriptwars.game.Player;
import brownshome.scriptwars.game.tanks.Coordinates;

import brownshome.scriptwars.game.snake.SnakeGameDisplayHandler.SnakeHeadItem;
import brownshome.scriptwars.game.snake.SnakeGameDisplayHandler.SnakeSegmentItem;
import brownshome.scriptwars.game.snake.SnakeGameDisplayHandler.FoodGridItem;

/**
 * This tracks the players snakes in the game.
 * Each player needs to be associated with a snake that is on the stage.
 * This class allows the tracking of snake objects on the stage,
 * and links them to their controlling players.
 */
public class World {
	
	public class WorldException extends Exception {
		private String info;
		public WorldException(String info) {
			this.info = info;
		}
	}

	private Map<Player<?>, AbstractSnake> snakes;
	private Map<AbstractSnake, Player<?>> owners;
	
	public World() {
		snakes = new HashMap<Player<?>, AbstractSnake>();
		owners = new HashMap<AbstractSnake, Player<?>>();
	}
	
	private void assertSnakeExists(Player<?> player) throws WorldException {
		if(!snakes.containsKey(player)) {
			throw new WorldException("Player's snake doesn't exist.");
		}
	}
	
	private void assertOwnerExists(AbstractSnake snake) throws WorldException {
		if(!owners.containsKey(snake)) {
			throw new WorldException("Snake's player doesn't exist.");
		}
	}
	
	public AbstractSnake getSnake(Player<?> player) throws WorldException {
		assertSnakeExists(player);
		return snakes.get(player);
	}
	
	public boolean isAlive(Player<?> player) throws WorldException {
		assertSnakeExists(player);
		return !snakes.get(player).destroyed();
	}
	
	public boolean hasSnake(Player<?> player) {
		return snakes.containsKey(player);
	}
	
	/**
	 * Puts a snake on the stage of the passed engine.
	 * @param player
	 * @param engine
	 */
	public void spawnSnake(Player<?> player, Engine engine) {
		// TODO make this choose a suitable spawn based on the map and current snakes, etc.
		int spawnx = 2;
		int spawny = 4;
		Direction spawnDirection = Direction.EAST;
		int spawnLength = 3;
		ISnakeModel model = new BasicSnakeModel(spawnx, spawny, spawnDirection, spawnLength);
		AbstractSnake snake = new Snake(model);
		engine.stage.addSnake(snake);
		snakes.put(player, snake);
		owners.put(snake, player);
	}
	
	public Player<?> getOwner(AbstractSnake snake) throws WorldException {
		assertOwnerExists(snake);
		return owners.get(snake);
	}

	protected void displayWorld(SnakeGameDisplayHandler handler, Engine engine) {
		// TODO make this render the world.
		
		Collection<GridItem> items = new ArrayList<>(); // Collection of items to be drawn.
		
		Coordinates position;
		
		Iterator<AbstractSnake> snakeIterator = engine.stage.getSnakeIterator();
		AbstractSnake snake;
		while(snakeIterator.hasNext()) {
			snake = snakeIterator.next();
			
			if(!snake.destroyed()) {
				position = new Coordinates(snake.model.getSegmentX(0), snake.model.getSegmentY(0));
				items.add(new SnakeHeadItem(position));
				for(int i = 1; i < snake.model.getLength(); i++) {
					position = new Coordinates(snake.model.getSegmentX(i),snake.model.getSegmentY(0));
					items.add(new SnakeSegmentItem(position));
				}
			}
		}
		
		Iterator<AbstractGameObject> goIterator = engine.stage.getGameObjectIterator();
		AbstractGameObject object;
		while(goIterator.hasNext()) {
			object = goIterator.next();
			
			if(!object.destroyed()) {
				position = new Coordinates(object.model.getX(),object.model.getY());
				items.add(handler.getRenderObject(position,object.model.getType()));
			}
		}
		
		handler.setDynamicItems(items);
		
		// for now we will just leave it empty, just draw static map.
		
		/*
		
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
		*/
	}
	
}
