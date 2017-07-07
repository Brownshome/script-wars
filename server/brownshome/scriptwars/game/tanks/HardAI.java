package brownshome.scriptwars.game.tanks;

import java.io.IOException;
import java.util.*;

public class HardAI {
	private TankAPI api;
	private Random rand = new Random();
	private LinkedHashSet<Coordinates> priority = new LinkedHashSet<>();
	private Coordinates last = null;
	private int ammo = 50;
	
	public static void main(String[] args) throws IOException {
		new HardAI(Integer.parseInt(args[0]));
	}
	
	private HardAI(int ID) throws IOException {
		api = new TankAPI(ID, "www.script-wars.com", "Hard AI");
		boolean first = true;
		
		while(api.nextTick()) {
			if(!api.isAlive()) {
				continue;
			}
			
			ammo++;
			ammo = Math.max(50, ammo);
			
			if(first) {
				fillPriorityList();
				first = false;
			}
			
			flagViewed();
			if(api.me().getPosition().equals(last)) {
				priority.clear();
				fillPriorityList();
			}
			
			last = api.me().getPosition();
			
			pathTo(getNextCoord());
			
			if(ammo >= 5)
				shootBadGuys();
			
			avoidBullets();
			
			if(api.getAction() == Action.SHOOT)
				ammo -= 5;
			
			//api.printAction();
		}
	}
	
	private void flagCoord(Coordinates coord) {
		priority.remove(coord);
		priority.add(coord);
	}
	
	//Needs to be updated with the new ruleset
	private void flagViewed() {
		World map = api.getMap();
		
		flagCoord(api.me().getPosition());
		
		for(Direction dir : Direction.values()) {
			Coordinates coord = api.me().getPosition();
			while(!map.isWall(coord = dir.move(coord))) {
				flagCoord(coord);
			}
		}
	}
	
	private Coordinates getNextCoord() {
		return priority.iterator().next();
	}
	
	private void fillPriorityList() {
		List<Coordinates> array = new ArrayList<>();
		
		World map = api.getMap();
		for(int x = 0; x < map.getWidth(); x++) {
			for(int y = 0; y < map.getHeight(); y++) {
				if(!map.isWall(x, y)) {
					array.add(new Coordinates(x, y));
				}
			}
		}
		
		Collections.shuffle(array);
		priority.addAll(array);
	}
	
	private void avoidBullets() {
		Coordinates coord = api.me().getPosition();
		
		if(api.getAction() == Action.MOVE) {
			if(!isSafe(api.getDirection().move(coord))) {
				api.doNothing();
			} else {
				return;
			}
		}
		
		if(isSafe(coord))
			return;
		
		for(Direction direction : Direction.values()) {
			Coordinates newCoord = direction.move(coord);
			World world = api.getMap();
			
			if(!world.isWall(newCoord) && world.getTank(newCoord) == null && isSafe(newCoord)) {
				api.move(direction);
				return;
			}
		}
		
		return;
	}

	private boolean isSafe(Coordinates newCoord) {
		for(Shot shot : api.getVisibleShots()) {
			Coordinates coord = shot.getDirection().move(shot.getPosition());
			for(int x = 0; x < Shot.SPEED; x++) {
				if(coord.equals(newCoord))
					return false;
				
				coord = shot.getDirection().move(coord);
			}
		}
		
		for(Direction direction : Direction.values()) {
			if(api.getMap().getTank(direction.move(newCoord)) != null) {
				return false;
			}
		}
		
		return true;
	}
	
	private void shootBadGuys() {
		int minDistance = Integer.MAX_VALUE;
		Coordinates me = api.me().getPosition();
		Direction direction = null;
		
		for(Tank tank : api.getVisibleTanks()) {
			int distance = getDistance(tank);
			Coordinates other = tank.getPosition();
			Direction dir = Direction.getDirection(other.getX() - me.getX(), other.getY() - me.getY());
		
			if(dir != null && distance < minDistance) {
				direction = dir;
				minDistance = distance;
			}
		}
		
		if(direction == null)
			return;
		
		api.shoot(direction);
	}
	
	private int getDistance(Tank tank) {
		return Math.abs(tank.getPosition().getX() - api.me().getPosition().getX()) +
				Math.abs(tank.getPosition().getY() - api.me().getPosition().getY());
	}
	
	private void pathTo(Coordinates aim) {
		if(aim.equals(api.me().getPosition())) {
			return;
		}
		
		World map = api.getMap();
		Set<Coordinates> traversed = new HashSet<>();
		Set<Coordinates> toTraverse = new HashSet<>();
		Set<Coordinates> traverse = new HashSet<>();
		traverse.add(aim);
		
		while(!traverse.isEmpty()) {
			for(Coordinates coord : traverse) {
				for(Direction dir : Direction.values()) {
					Coordinates next = dir.move(coord);
					
					if(api.me().getPosition().equals(next)) {
						api.move(dir.opposite());
						return;
					}
					
					if(!map.isWall(next) && map.getTank(next) == null && !traversed.contains(next)) {	
						toTraverse.add(next);
					}
				}
			}
			
			traversed.addAll(traverse);
			traverse = toTraverse;
			toTraverse = new HashSet<>();
		}
		
		//We can't find a way, go to another spot
		flagCoord(aim);
	}
}
