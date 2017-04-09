package brownshome.scriptwars.client.tankapi;

public class Coordinate {
	public final int x;
	public final int y;

	public Coordinate(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Coordinate)) {
			return false;
		}
		
		Coordinate other = (Coordinate) obj;

		return other.x == x && other.y == y;
	}

	@Override
	public int hashCode() {
		return x ^ (y * 31); //TODO distribute high order bits better
	}
}