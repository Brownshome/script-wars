package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.GridItem;
import brownshome.scriptwars.game.Player;

public class Tank {
	/*
	 * Tank tick process
	 * 
	 * setNextAction
	 * 
	 * finalizeMove
	 * finalizeShot
	 * RemoveIfDead
	 */
	
	
	public final static int MAX_AMMO = 10;
		
	//the owner of this tank
	private Player<?> owner;
	//the direction of the last move
	private Direction direction;
	//the action to be taken next turn
	private Action action = Action.NOTHING;
	//the world that contains this tank
	private World world;
	private Coordinates position;
	private int ammo = MAX_AMMO;
	private boolean shouldRenderMove = false;
	private boolean isDead = false;
	
	//An ID that is only used on the client to identify
	private int clientID;
	
	/** Creates a tank on the client side */
	public Tank(Network network) {
		position = new Coordinates(network);
		clientID = network.getByte();
	}
	
	/** Creates a tank and adds it to the world map */
	protected Tank(Coordinates coordinates, Player<?> owner, World world) {
		this.position = coordinates;
		this.owner = owner;
		this.world = world;
	}
	
	protected void setNextAction(Direction direction, Action action) {
		this.direction = direction;
		this.action = action;
		
		switch(action) {
		case MOVE:
			preTickMove();
			break;
		case SHOOT:
			preTickShoot();
			break;
		}
	}
	
	private void preTickShoot() {
		Coordinates spaceToShoot = direction.move(position);
		if(world.isWall(spaceToShoot) || !removeAmmo()) {
			clearAction();
		}
	}

	/** Moves then tank to it's next position and sets the hasMoved flag */
	private void preTickMove() {
		shouldRenderMove = true;
		
		Coordinates newCoord = direction.move(position);
		if(world.isWall(newCoord)) {
			clearAction();
			return;
		}
		
		position = newCoord;
	}

	private void clearAction() {
		action = Action.NOTHING;
	}
	
	/** Places fired shots on the map, after this method, all shots will have been placed. Any shots that get shot
	 * are removed and this method returns true if this tank is immediately shot. */
	protected void finalizeShot() {
		//Fires a shot if there is no other tank shooting into the space where the shot would go.
		//This method also kills any tanks that get shot immediately
		boolean canShoot = true;
		boolean hasBeenShot = false;
		Coordinates coordinatesOfShot = direction.move(position);
		
		Tank shooter = null;
		for(Direction dir : Direction.values()) {
			Tank tank = world.getTank(dir.move(position));
			if(tank != null && tank.action == Action.SHOOT && tank.direction == dir.opposite()) {
				//We got shot
				world.addDeadGridItem(Shot.makeVirtualGridItem(position, coordinatesOfShot));
				
				if(!hasBeenShot) {
					shooter = tank;
					hasBeenShot = true;
				} else {
					shooter = null;
				}
			}
			
			tank = world.getTank(dir.move(coordinatesOfShot));
			if(tank != null && tank.action == Action.SHOOT && tank.direction == dir.opposite()) {
				canShoot = false;
			}
		}
		
		if(canShoot) {
			world.addShotToMap(new Shot(coordinatesOfShot, this, world, direction));
		}
		
		if(hasBeenShot && shooter != null) {
			kill();
			shooter.awardKill();
		}
	}
	
	protected void awardKill() {
		owner.addScore(1);
		refilAmmo();
	}

	/**
	 * Moves the tank if possible. This also sets this also rolls back the tanks if they are colliding.
	 */
	protected boolean finalizeMove() {
		//Checks if the tank's move is valid and rolls it back if it is not.
		
		//Check for tanks in the space we are trying to move into, if it is not us we need to move back ourselves and the other colliding tank.
		//The tank map gets rebuilt after all tanks have rolled back
		
		//We must do this for stationary tanks as they may be overiden by moving tanks.
		//Also swapping tanks need to be checked for
		
		Tank other = world.getTank(position);
		if(other == this) {
			//Check for swapping if we moved
			if(action == Action.MOVE) {
				Coordinates oldPosition = direction.opposite().move(position);
				Tank swap = world.getTank(oldPosition);
				
				if(swap.direction.opposite() == direction) {
					assert action == Action.MOVE || swap.action == Action.MOVE;
					
					rollBack();
					swap.rollBack();
					
					return true;
				}
			}
			
			return false;
		}
		
		assert action == Action.MOVE || other.action == Action.MOVE;
		
		other.rollBack();
		rollBack();
		return true;
	}
	
	protected void removeIfDead() {
		if(isDead) world.removeTank(this);
	}

	protected void doAmmoPickups() {
		if(world.removeAmmoPickup(position))
			refilAmmo();
	}
	
	protected void kill() {
		isDead = true;
		world.addDeadGridItem(getRenderItem());
		owner.addScore(-1);
	}

	@Override
	public String toString() {
		return "P:" + position + (owner == null ? "" : " T: " + owner.getSlot());
	}

	public Coordinates getPosition() {
		return position;
	}

	private void rollBack() {
		if(action != Action.MOVE)
			return;
		
		position = direction.opposite().move(position);
		shouldRenderMove = false;
		clearAction();
	}

	private boolean removeAmmo() {
		if(ammo >= 1) {
			ammo -= 1;
			return true;
		}
		
		return false;
	}

	protected GridItem getRenderItem() {
		return new GridItem() {
			private final Coordinates start, end;
			private final byte code;
			
			{
				if(shouldRenderMove) {
					Direction dir = direction.opposite();
					end = position;
					start = dir.move(end);
					shouldRenderMove = false;
				} else {
					start = end = position;
				}
				
				TankGame game = (TankGame) owner.getGame();
				code = (byte) (game.getIndex(owner) + TankGameDisplayHandler.DynamicSprites.TANK_START.ordinal());
			}
			
			@Override public byte code() { return code; }
			@Override public Coordinates start() { return start; }
			@Override public Coordinates end() { return end; }
		};
	}
	
	private void refilAmmo() {
		ammo = MAX_AMMO;
	}
	
	/** Returns a unique ID that can be used to identify tanks. */
	public int clientID() {
		return clientID;
	}

	protected Player<?> getOwner() {
		return owner;
	}

	protected byte ammo() {
		return (byte) ammo;
	}
}
