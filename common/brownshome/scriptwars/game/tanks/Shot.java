package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.client.*;
import brownshome.scriptwars.game.tanks.Direction;

public class Shot {
	public static final char BULLET = 2;
	public static final int SPEED = 3;
	
	private Tank owner;
	private World world;
	private Direction direction;
	private Coordinates position;
	
	protected Shot(Coordinates spawn, Tank owner, World world, Direction direction) {
		this.position = spawn;
		this.owner = owner;
		this.world = world;
		this.direction = direction;
	}

	public Shot(Network network) {
		position = new Coordinates(network);
		direction = Direction.values()[network.getByte()];
	}
	
	/**
	 * Returns true if the shot should be destroyed
	 **/
	protected boolean tickShot() {
		for(int i = 0; i < SPEED; i++) {
			position = direction.move(position);
			
			Tank tank = world.getTank(position);
			
			if(tank != null) {
				tank.kill();
				owner.getOwner().addScore(1);
				owner.returnAmmo();
				return true;
			}
			
			if(world.isWall(position)) {
				owner.returnAmmo();
				return true;
			}
		}
		
		return false;
	}

	public Tank getOwner() {
		return owner;
	}
	
	public Coordinates getPosition() {
		return position;
	}

	public Direction getDirection() {
		return direction;
	}
}
