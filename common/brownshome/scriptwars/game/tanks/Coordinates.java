package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.client.Network;

public class Coordinates {
	private final int x;
	private final int y;
	
	public Coordinates(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Coordinates(Network network) {
		this(network.getByte(), network.getByte()); //The execution order here is guaranteed
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Coordinates)) {
			return false;
		}
		
		Coordinates other = (Coordinates) obj;

		return other.x == x && other.y == y;
	}

	@Override
	public int hashCode() {
		return x ^ (y * 31); //TODO distribute high order bits better
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public static Coordinates getRandom(int maxX, int maxY) {
		return new Coordinates((int) (Math.random() * maxX), (int) (Math.random() * maxY));
	}
}