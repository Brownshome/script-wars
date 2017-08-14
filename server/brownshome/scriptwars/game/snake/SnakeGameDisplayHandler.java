package brownshome.scriptwars.game.snake;

import com.liamtbrand.snake.model.IGameObjectModel.Type;

import brownshome.scriptwars.game.Game;
import brownshome.scriptwars.game.GridDisplayHandler;
import brownshome.scriptwars.game.GridItem;
import brownshome.scriptwars.game.tanks.Coordinates;

public class SnakeGameDisplayHandler extends GridDisplayHandler {

	public SnakeGameDisplayHandler(Game game) {
		super(game);
	}
	
	public static enum StaticSprites {
		NOTHING, WALL;
	}
	
	public static enum GridItemSprites {
		UNRECOGNISED, FOOD, SNAKE_HEAD, SNAKE_SEGMENT;
	}
	
	public static abstract class AbstractPositionedGridItem implements GridItem {
		protected Coordinates position;
		protected byte code;
		public AbstractPositionedGridItem(Coordinates position, byte code) {
			this.position = position;
			this.code = code;
		}
		public byte code() { return code; }
		public Coordinates start() { return position; }
		public Coordinates end() { return position; } // TODO : Why is this not tracked by the display class?
	}
	
	public static class UnrecognisedGridItem extends AbstractPositionedGridItem {

		public UnrecognisedGridItem(Coordinates position) {
			super(position, (byte) GridItemSprites.UNRECOGNISED.ordinal());
		}
	}
	
	public static class FoodGridItem extends AbstractPositionedGridItem {
		public FoodGridItem(Coordinates position) {
			super(position, (byte) GridItemSprites.FOOD.ordinal());
		}
	}
	
	public static class SnakeHeadItem extends AbstractPositionedGridItem {
		public SnakeHeadItem(Coordinates position) {
			super(position, (byte) GridItemSprites.SNAKE_HEAD.ordinal());
		}
	}
	
	public static class SnakeSegmentItem extends AbstractPositionedGridItem {
		public SnakeSegmentItem(Coordinates position) {
			super(position, (byte) GridItemSprites.SNAKE_SEGMENT.ordinal());
		}
	}

	public GridItem getRenderObject(Coordinates position,Type type) {
		switch (type) {
			default:
				return new UnrecognisedGridItem(position);
		}
	}

}
