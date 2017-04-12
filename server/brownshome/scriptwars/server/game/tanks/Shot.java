package brownshome.scriptwars.server.game.tanks;

public class Shot {
	static final char BULLET = 2;
	public static final int SPEED = 1;
	
	Tank owner;
	World world;
	Direction direction;
	int x;
	int y;
	
	Shot(int x, int y, Tank owner, World world, Direction direction) {
		this.x = x;
		this.owner = owner;
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
				owner.owner.addScore(1);
				owner.returnAmmo();
				return true;
			}
			
			if(world.isWall(x, y)) {
				owner.returnAmmo();
				return true;
			}
		}
		
		return false;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public Direction getDirection() {
		return direction;
	}
}
