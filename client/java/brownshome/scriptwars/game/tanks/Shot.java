package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.GridItem;

public class Shot {
	/*
	 * updatePrevious
	 * tickShot
	 * detectCollisions
	 * completeTick
	 */
	
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
	
	private boolean isDead = false;
	private boolean didDieSwapping = false;
	
	/**
	 * Returns true if the shot should be destroyed, this method defers the actual killing of the tanks later so
	 * that the list of shots can be iterated through without issues.
	 **/
	protected void tickShot() {
		position = direction.move(position);

		if(world.isWall(position)) {
			world.addDeadGridItem(getRenderItem());
			isDead = true;
			return;
		}

		world.addShotToMap(this);
	}
	
	/** Returns true if this shot should be deleted */
	protected void detectCollisions() {
		Shot other = world.getShot(position);
		if(other == this) {
			//Check for swapping if we moved
			Coordinates oldPosition = direction.opposite().move(position);
			Shot swap = world.getShot(oldPosition);

			if(swap != null && swap.direction.opposite() == direction) {
				isDead = true;
				swap.isDead = true;
				
				//To avoid the shots swapping over each other we stop both of them at this position. This will
				//need to be changed later if we add fractional rendering
				didDieSwapping = true;
				swap.didDieSwapping = true;
				
				return;
			}

			return;
		}

		isDead = true;
		other.isDead = true;
	}
	
	/** Kills any tanks that would be shot */
	protected void completeTick() {
		if(isDead) {
			if(!didDieSwapping) {
				world.addDeadGridItem(getRenderItem());
			} else {
				position = direction.opposite().move(position);
				if(!position.equals(previous)) {
					world.addDeadGridItem(getRenderItem());
				}
			}
			
			world.removeShotFromMap(this);
			return;
		}
		
		Tank tank = world.getTank(position);
		if(tank != null) {
			world.removeTank(tank);
			tank.kill();
			owner.awardKill();
			
			isDead = true;
			completeTick();
		}
	}
	
	protected void updatePrevious() {
		previous = position;
	}
	
	protected Tank getOwner() {
		return owner;
	}
	
	public Coordinates getPosition() {
		return position;
	}

	public Direction getDirection() {
		return direction;
	}

	protected GridItem getRenderItem() {
		return new GridItem() {
			@Override public byte code() { return (byte) TankGameDisplayHandler.DynamicSprites.SHOT.ordinal(); }
			@Override public Coordinates start() { return previous; }
			@Override public Coordinates end() { return position; }
		};
	}

	protected static GridItem makeVirtualGridItem(Coordinates from, Coordinates to) {
		return new GridItem() {
			@Override public byte code() { return (byte) TankGameDisplayHandler.DynamicSprites.SHOT.ordinal(); }
			@Override public Coordinates start() { return from; }
			@Override public Coordinates end() { return to; }
		};
	}
}
