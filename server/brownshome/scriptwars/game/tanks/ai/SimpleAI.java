package brownshome.scriptwars.game.tanks.ai;

import java.io.IOException;

import brownshome.scriptwars.game.tanks.*;

//Follow the left wall. Shoot and dodge.
public class SimpleAI extends ServerAI {
	Direction dir = Direction.UP;
	Coordinates old = null;
	
	SimpleAI(String[] args) throws IOException {
		super(args);
		
		while(api.nextTick()) {
			if(!api.isAlive()) {
				old  = null;
				continue;
			}
			
			if(old != null && old.equals(api.getCurrentPosition())) {
				dir = dir.clockwise();
			} else {
				Direction left = dir.antiClockwise();
				Direction back = dir.opposite();
				Direction right = dir.clockwise();
				Coordinates c = api.getCurrentPosition();

				//If there is a backleft block and a left space go left
				//If there is a forward space go forward
				//If there is a right sapce go right
				//Go back

				if(!isSpace(left.move(back.move(c))) && isSpace(left.move(c))) {
					dir = left;
				} else if(isSpace(dir.move(c))) {

				} else if(isSpace(right.move(c))) {
					dir = right;
				} else {
					dir = back;
				}
			}
			
			api.move(dir);
			old = api.getCurrentPosition();
			
			shootBadGuys();
			avoidDirectBullets();
		}
	}

	private boolean isSpace(Coordinates c) {
		return !api.getMap().isWall(c) && api.getMap().getTank(c) == null;
	}

	@Override
	boolean hasNearbyTank(Coordinates other) {
		return false;
	}

	public static void main(String[] args) throws IOException {
		new SimpleAI(args);
	}

	@Override
	String name() {
		return "Simple AI";
	}
}
