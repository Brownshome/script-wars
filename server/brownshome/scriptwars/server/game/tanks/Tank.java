package brownshome.scriptwars.server.game.tanks;

import brownshome.scriptwars.server.game.Player;

public class Tank {
	public final static int MAX_AMMO = 5;
	
	Player owner;
	//the direction of the last move
	Direction direction;
	World world;
	int x;
	int ammo = MAX_AMMO;
	int y;
	boolean hasMoved = false;
	
	public Tank(int x, int y, Player owner, World world) {
		this.x = x;
		this.y = y;
		this.owner = owner;
		this.world = world;
	}

	Direction getDirection() {
		return direction;
	}
	
	void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	/**
	 * @param direction
	 * @return true if the movement was successful
	 */
	boolean move(Direction direction) {
		this.direction = direction;
		
		int newX = direction.moveX(x);
		int newY = direction.moveY(y);
		
		Tank tank = world.getTank(newX, newY);
		if(tank != null) {
			if(tank.hasMoved) {
				world.addToRewindList(tank);
			}
			
			return false;
		}
		
		if(world.isWall(newX, newY)) {
			return false;
		}
		
		x = newX;
		y = newY;
		hasMoved = true;
		
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")T: " + owner.getSlot();
	}
	
	void kill() {
		owner.addScore(-1);
		world.removeTank(this);
	}

	int getX() {
		return x;
	}

	int getY() {
		return y;
	}

	void rollBack() {
		x -= direction.dx;
		y -= direction.dy;
		return;
	}

	Player getOwner() {
		return owner;
	}

	char getCharacter() {
		return (char) (hashCode() % 25 + 'A');
	}

	boolean removeAmmo() {
		if(ammo > 0) {
			ammo--;
			return true;
		}
		
		return false;
	}

	public void returnAmmo() {
		if(ammo < MAX_AMMO) {
			ammo++;
		}
	}
}
