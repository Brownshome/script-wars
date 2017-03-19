package brownshome.scriptwars.server.game;

public class DisplayHandler {
	char[][] grid;
	
	public void setGridSize(int x, int y) {
		grid = new char[x][y];
	}
	
	public void print() {
		System.out.println("Game Grid:");
		
		for(char[] row : grid) {
			System.out.print('|');
			
			for(char c : row) {
				System.out.print(c);
			}
			
			System.out.println('|');
		}
	}
	
	public void putGrid(char[][] grid) {
		this.grid = grid;
	}
	
	/**
	 * @param width
	 * @param height
	 * @param x The position of the upper left corner
	 * @param y The position of the upper left corner
	 * @param character
	 */
	public void putSquare(float width, float height, float x, float y, char character) {
		for(int yCoord = (int) (grid.length * y); yCoord >= 0 && yCoord < grid.length; yCoord++) {
			for(int xCoord = (int) (grid[yCoord].length * x); xCoord >= 0 && xCoord < grid[yCoord].length; xCoord++) {
				grid[yCoord][xCoord] = character;
			}
		}
	}
}
