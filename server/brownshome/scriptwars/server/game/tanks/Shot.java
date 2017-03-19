package brownshome.scriptwars.server.game.tanks;

class Shot {
	static final char BULLET = '•';
	static final int SPEED = 1;
	
	World world;
	Direction direction;
	int x;
	int y;
	
	Shot(int x, int y, World world, Direction direction) {
		this.x = x;
		this.y = y;
		this.world = world;
		this.direction = direction;
	}
	
	/**
	 * Returns true if the shot should be destroyed
	 **/
	boolean tickShot() {
		for(int i = 0; i < SPEED; i++) {
			x = direction.moveX(x);
			y = direction.moveY(y);
			
			Tank tank = world.getTank(x, y);
			
			if(tank != null) {
				tank.kill();
				return true;
			}
			
			if(world.isWall(x, y)) {
				return true;
			}
		}
		
		return false;
	}
}
