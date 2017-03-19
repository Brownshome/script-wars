package brownshome.scriptwars.server.game.tanks;

import brownshome.scriptwars.server.game.Player;

class Tank {
	Player owner;
	//the direction of the last move
	Direction direction;
	World world;
	int x;
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
	
	void serDirection(Direction direction) {
		this.direction = direction;
	}
	
	/**
	 * @param direction
	 * @return true if the movement was successful
	 */
	boolean move(Direction direction) {
		this.direction = direction;
		
		hasMoved = false;
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
	
	void kill() {
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
	}

	Player getOwner() {
		return owner;
	}

	char getCharacter() {
		return (char) (hashCode() % 25 + 'A');
	}
}
