package brownshome.scriptwars.game.snakes;

import com.liamtbrand.snake.model.IMapModel;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.Coordinates;

public class BasicMapModel implements IMapModel {
	private final boolean[][] data;
	private final int width;
	private final int height;
	
	public BasicMapModel(Network network) {
		width = network.getByte();
		height = network.getByte();
		data = new boolean[height][width];
		
		for(int y = 0; y < width; y++) {
			for(int x = 0; x < height; x++) {
				data[y][x] = network.getBoolean();
			}
		}
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public boolean isWall(int x, int y) {
		return data[y][x];
	}
	
	public boolean isWall(Coordinates coord) {
		return isWall(coord.getX(), coord.getY());
	}
}
