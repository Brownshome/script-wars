package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.connection.Network;

public class Shot {
	public static final char BULLET = 2;
	public static final int SPEED = 3;
	private Tank owner;
	private World world;
	private Direction direction;
	private Coordinates position;
	private Coordinates previous;
	
	protected Shot(Coordinates spawn, Tank owner, World world, Direction direction) {
		this.position = spawn;
		this.owner = owner;
		this.world = world;
		this.direction = direction;
		previous = owner.getPosition();
	}

	public Shot(Network network) {
		position = new Coordinates(network);
		direction = Direction.values()[network.getByte()];
	}
	
	/**
	 * Returns true if the shot should be destroyed, this method defers the actual killing of the tanks later so
	 * that the list of shots can be iterated through without issues.
	 **/
	protected boolean tickShot() {
		position = direction.move(position);

		Tank tank = world.getTank(position);

		if(tank != null) {
			return true;
		}

		if(world.isWall(position)) {
			return true;
		}
		
		return false;
	}

	protected void updatePrevious() {
		previous = position;
	}
	
	protected void completeTick() {
		Tank tank = world.getTank(position);
		if(tank != null) {
			tank.kill();
			owner.getOwner().addScore(1);
			owner.refilAmmo();
		}
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

	public Coordinates getPrevious() {
		return previous;
	}
}
