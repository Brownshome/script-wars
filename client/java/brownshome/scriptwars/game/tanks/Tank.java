package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.Player;

public class Tank {
	public final static int MAX_AMMO = 10;
		
	private Player<?> owner;
	//the direction of the last move
	private Direction direction;
	private World world;
	private Coordinates position;
	private int ammo = MAX_AMMO;
	private boolean hasMoved = false;
	private int clientID;
	
	public Tank(Coordinates coordinates) {
		position = coordinates;
	}
	
	public Tank(Network network) {
		position = new Coordinates(network);
		clientID = network.getByte();
	}
	
	protected Tank(Coordinates coordinates, Player<?> owner, World world) {
		this.position = coordinates;
		this.owner = owner;
		this.world = world;
	}

	protected Direction getDirection() {
		return direction;
	}
	
	protected void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	/**
	 * @param direction
	 * @return true if the movement was successful
	 */
	protected boolean move(Direction direction) {
		this.direction = direction;
		
		Coordinates newCoord = direction.move(position);
		
		if(world.isWall(newCoord)) {
			return false;
		}
		
		position = newCoord;
		hasMoved = true;
		
		return true;
	}
	
	protected void kill() {
		owner.addScore(-1);
		world.removeTank(this);
		world.deadTanksToRender.add(this);
	}

	@Override
	public String toString() {
		return "P:" + position + (owner == null ? "" : " T: " + owner.getSlot());
	}

	public Coordinates getPosition() {
		return position;
	}

	protected void rollBack() {
		if(!hasMoved)
			return;
		
		position = direction.opposite().move(position);
		hasMoved = false;
		return;
	}

	protected Player<?> getOwner() {
		return owner;
	}

	protected boolean removeAmmo() {
		if(ammo >= 1) {
			ammo -= 1;
			return true;
		}
		
		return false;
	}

	protected void refilAmmo() {
		ammo = MAX_AMMO;
	}

	protected void clearHasMoved() {
		hasMoved = false;
	}

	protected boolean hasMoved() {
		return hasMoved;
	}
	
	public int clientID() {
		return clientID;
	}

	public int ammo() {
		return ammo;
	}
}
