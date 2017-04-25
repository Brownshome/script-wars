package brownshome.scriptwars.game;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public abstract class DisplayHandler {
	protected Set<Consumer<ByteBuffer>> viewers = new HashSet<>();
	protected Set<Consumer<ByteBuffer>> newViewers = new HashSet<>();
	
	private final ReentrantReadWriteLock displayLock = new ReentrantReadWriteLock();
	
	
	
	public synchronized void addViewer(Consumer<ByteBuffer> viewer) {
		newViewers.add(viewer);
	}

	public synchronized void removeViewer(Consumer<ByteBuffer> viewer) {
		viewers.remove(viewer);
	}

	ReentrantReadWriteLock getLock() {
		return displayLock;
	}

	public abstract void print();
}
