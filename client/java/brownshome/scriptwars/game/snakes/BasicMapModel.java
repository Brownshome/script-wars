package brownshome.scriptwars.game.snakes;

import com.liamtbrand.snake.model.IMapModel;

public class BasicMapModel implements IMapModel {
	
	public int data[][];
	public int width;
	public int height;
	
	public BasicMapModel(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new int[width][height];
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
		return ( data[x][y] == 1 ? true : false );
	}
	
	public void setWall(int x, int y, boolean isWall) {
		data[x][y] = ( isWall ? 1 : 0 );
	}
}
