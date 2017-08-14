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
		FOOD, SNAKE_HEAD, SNAKE_SEGMENT, WORMHOLE;
	}
	
	public static class PositionedGridItem implements GridItem {
		protected Coordinates position;
		protected byte code;
		public PositionedGridItem(Coordinates position, byte code) {
			this.position = position;
			this.code = code;
		}
		public byte code() { return code; }
		public Coordinates start() { return position; }
		public Coordinates end() { return position; } // TODO : Why is this not tracked by the display class?
	}
	
	public static class SnakeHeadItem extends PositionedGridItem {
		public SnakeHeadItem(Coordinates position) {
			super(position, (byte) GridItemSprites.SNAKE_HEAD.ordinal());
		}
	}
	
	public static class SnakeSegmentItem extends PositionedGridItem {
		public SnakeSegmentItem(Coordinates position) {
			super(position, (byte) GridItemSprites.SNAKE_SEGMENT.ordinal());
		}
	}

	public GridItem getGridItem(Coordinates position,Type type) {
		switch (type) {
			case FOOD:
				return new PositionedGridItem(position, (byte) GridItemSprites.FOOD.ordinal());
			case WORMHOLE:
				return new PositionedGridItem(position, (byte) GridItemSprites.WORMHOLE.ordinal());
			default:
				return null;
		}
	}

}
