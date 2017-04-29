package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.client.*;
import brownshome.scriptwars.game.Player;
import brownshome.scriptwars.game.tanks.Direction;

public class Tank {
	public final static int MAX_AMMO = 5;
	
	private Player owner;
	//the direction of the last move
	private Direction direction;
	private World world;
	private Coordinates position;
	private int ammo = MAX_AMMO;
	private boolean hasMoved = false;
	
	public Tank(Network network) {
		int x = network.getByte();
		int y = network.getByte();
		position = new Coordinates(x, y);
	}
	
	protected Tank(Coordinates coordinates, Player owner, World world) {
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

	protected Player getOwner() {
		return owner;
	}

	protected char getCharacter() {
		return (char) (hashCode() % 25 + 'A');
	}

	protected boolean removeAmmo() {
		if(ammo > 0) {
			ammo--;
			return true;
		}
		
		return false;
	}

	protected void returnAmmo() {
		if(ammo < MAX_AMMO) {
			ammo++;
		}
	}

	protected void clearHasMoved() {
		hasMoved = false;
	}

	protected boolean hasMoved() {
		return hasMoved;
	}
}
