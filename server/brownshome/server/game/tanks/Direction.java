package brownshome.server.game.tanks;

enum Direction {
	UP(0, 1),
	DOWN(0, -1),
	LEFT(-1, 0),
	RIGHT(1, 0);
	
	final int dx, dy;
	
	Direction(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public int moveX(int x) {
		return x + dx;
	}
	
	public int moveY(int y) {
		return y + dy;
	}
}
