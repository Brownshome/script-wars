package brownshome.scriptwars.game.tanks;

import java.io.IOException;
import java.util.*;

public class HardAI {
	private TankAPI api;
	private Random rand = new Random();
	private LinkedHashSet<Coordinates> priority = new LinkedHashSet<>();
	
	public static void main(String[] args) throws IOException {
		new HardAI(Integer.parseInt(args[0]));
	}
	
	private HardAI(int ID) throws IOException {
		api = new TankAPI(ID, "www.script-wars.com", "Hard AI");
		boolean first = true;
		
		while(api.nextTick()) {
			if(!api.isAlive()) {
				System.out.println("We are dead");
				continue;
			}
			
			if(first) {
				fillPriorityList();
				first = false;
			}
			
			flagViewed();
			while(pathTo(getNextCoord()));
			
			shootBadGuys();
			avoidBullets();
			
			//api.printAction();
		}
		
		System.out.println(api.getConnectionStatus());
	}
	
	private void flagCoord(Coordinates coord) {
		priority.remove(coord);
		priority.add(coord);
	}
	
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
		World map = api.getMap();
		for(int x = 0; x < map.getWidth(); x++) {
			for(int y = 0; y < map.getHeight(); y++) {
				if(!map.isWall(x, y)) {
					priority.add(new Coordinates(x, y));
				}
			}
		}
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
		
		//we are going to die
		System.out.println("We are going to die");
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
	
	private boolean pathTo(Coordinates aim) {
		if(aim.equals(api.me().getPosition())) {
			System.out.println("Reached goal");
			return true;
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
					
					if(!map.isWall(next) && !traversed.contains(next)) {
						if(api.me().getPosition().equals(next)) {
							api.move(dir.opposite());
							return false;
						}
						
						toTraverse.add(next);
					}
				}
			}
			
			traversed.addAll(traverse);
			traverse = toTraverse;
			toTraverse = new HashSet<>();
		}
		
		System.out.println("No path found");
		return false;
	}
}
