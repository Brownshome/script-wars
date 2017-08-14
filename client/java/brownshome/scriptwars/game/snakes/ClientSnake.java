package brownshome.scriptwars.game.snakes;

import java.util.Iterator;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.Coordinates;

public class ClientSnake implements Iterable<Coordinates> {
	private final Coordinates[] segments;
	private final int id;
	
	public ClientSnake(Network network) {
		id = network.getInt();
		segments = new Coordinates[network.getByte()];
		for(int i = 0; i < segments.length; i++) {
			segments[i] = new Coordinates(network);
		}
	}

	public Coordinates getHead() {
		return segments[0];
	}
	
	public Coordinates getTail() {
		return segments[segments.length - 1];
	}
	
	public int getLength() {
		return segments.length;
	}
	
	public int getID() {
		return id;
	}
	
	@Override
	public Iterator<Coordinates> iterator() {
		return new Iterator<Coordinates>() {
			private int i = 0;
			@Override public boolean hasNext() { return i < segments.length; }
			@Override public Coordinates next() { return segments[i++]; }
		};
	}
}
