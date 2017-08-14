package brownshome.scriptwars.game.tanks.ai;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import brownshome.scriptwars.game.Coordinates;
import brownshome.scriptwars.game.Direction;
import brownshome.scriptwars.game.tanks.*;

//Shoot bad guys
//Avoid imminent death
//Avoid eventual death
//Seek ammo
//Seek bullets
//Pathfind to
abstract class ServerAI {
	final TankAPI api;
	final Random random = new Random();
	
	ServerAI(String[] args) throws IOException {
		int id;
		
		if(args.length == 0) {
			System.out.print("ID: ");
			id = (int) new Scanner(System.in).nextLong();
		} else {
			id = Integer.parseInt(args[0]);
		}
		
		api = new TankAPI(id, "localhost", name());
	}
	
	abstract String name();
	
	final boolean pathTo(Predicate<Coordinates> test) {
		Map<Coordinates, Direction> link = new HashMap<>();
		Set<Coordinates> toBeExplored = new HashSet<>();
		toBeExplored.add(api.getCurrentPosition());
		Set<Coordinates> explored = new HashSet<>();
		int timeFromNow = 0;
		
		while(!toBeExplored.isEmpty()) {
			timeFromNow++;
			Set<Coordinates> tmp = toBeExplored;
			toBeExplored = new HashSet<>();
			
			for(Coordinates c : tmp) {
				for(Direction d : Direction.values()) {
					Coordinates next = d.move(c);
					if(test.test(next)) {
						//Start backtrack
						Coordinates current = next;
						while(true) {
							Direction back = link.get(current).opposite();
							current = back.move(current);
							
							if(current.equals(api.getCurrentPosition())) {
								api.move(back.opposite());
								return true;
							}
						}
					}
					
					if(isValidPath(timeFromNow, next))
						toBeExplored.add(next);
				}
			}
		}
		
		return false;
	}
	
	boolean isValidPath(int timeFromNow, Coordinates c) { 
		return !api.getMap().isWall(c) && api.getMap().getTank(c) == null;
	}

	final void shootBadGuys() {
		if(api.getAmmo() == 0)
			return;
		
		for(Tank tank : api.getVisibleTanks()) {
			Direction dir;
			if((dir = Direction.getDirection(tank.getPosition(), api.getCurrentPosition())) != null) {
				api.shoot(dir);
				return;
			}
		}
	}
	
	final void avoidDirectBullets() {
		//Avoid bullets
		//Shoot bullets
		//Avoid people
		
		Coordinates currentPosition = api.getCurrentPosition();
		Set<Direction> safeDirections = new HashSet<>();
		Set<Direction> semiSafeDirections = new HashSet<>();
		
		if(!hasIncommingShots(currentPosition))
			semiSafeDirections.add(null);
		
		for(Direction d : Direction.values()) {
			if(!hasIncommingShots(d.move(currentPosition)))
				semiSafeDirections.add(d);
		}
		
		for(Direction d : semiSafeDirections) {
			Coordinates other = d == null ? currentPosition : d.move(currentPosition);
			if(!hasNearbyTank(other)) {
				safeDirections.add(d);
			}
		}
		
		if(safeDirections.contains(api.getDirection()))
			return;
		
		if(!safeDirections.isEmpty()) {
			if(safeDirections.contains(null))
				api.doNothing();
			else {
				Direction[] directions = safeDirections.toArray(new Direction[0]);
				api.move(directions[random.nextInt(directions.length)]);
			}
			
			return;
		}
		
		if(!semiSafeDirections.isEmpty()) {
			if(semiSafeDirections.contains(null))
				api.doNothing();
			else {
				Direction[] directions = semiSafeDirections.toArray(new Direction[0]);
				api.move(directions[random.nextInt(directions.length)]);
			}
			
			return;
		}
		
		//We are dead
	}
	
	boolean hasNearbyTank(Coordinates other) {
		for(Tank t : api.getVisibleTanks()) {
			if(isNextTo(t.getPosition(), other))
				return true;
		}
		
		return false;
	}

	private boolean isNextTo(Coordinates a, Coordinates b) {
		int dx = a.getX() - b.getX();
		int dy = a.getY() - b.getY();
		
		return (dx == 1 || dx == -1) && dy == 0 || (dy == 1 || dy == -1) && dx == 0;
	}

	private boolean hasIncommingShots(Coordinates position) {
		if(api.getMap().isWall(position) && api.getMap().getTank(position) != null)
			return true;
		
		for(Shot shot : api.getVisibleShots()) {
			Coordinates p = shot.getPosition();
			for(int i = 0; i < Shot.SPEED; i++) {
				p = shot.getDirection().move(p);
				if(p.equals(position))
					return true;
			}
		}
		
		return false;
	}

	final void avoidEventualBullets() {
		pathTo(c -> isSafeInFuture(c));
	}
	
	private boolean isSafeInFuture(Coordinates coord) {
		for(Direction d : Direction.values()) {
			for(Coordinates c = coord; !api.getMap().isWall(c); c = d.move(c)) {
				Shot shot = api.getMap().getShot(c);
				if(shot != null && shot.getDirection() == d.opposite())
					return false;
			}
		}
		
		return true;
	}

	final void seekAmmo() {
		pathTo(c -> api.getMap().getAmmoPickups().contains(c));
	}
}
