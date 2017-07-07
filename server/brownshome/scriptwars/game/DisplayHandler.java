package brownshome.scriptwars.game;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.*;
import java.util.function.Consumer;

public abstract class DisplayHandler {
	protected Set<Consumer<ByteBuffer>> viewers = new CopyOnWriteArraySet<>();
	protected Set<Consumer<ByteBuffer>> newViewers = new HashSet<>();
	
	private final ReentrantLock viewerListLock = new ReentrantLock();
	
	public void addViewer(Consumer<ByteBuffer> viewer) {
		getLock().lock();
		newViewers.add(viewer);
		getLock().unlock();
	}

	public void removeViewer(Consumer<ByteBuffer> viewer) {
		viewers.remove(viewer);
	}

	protected ReentrantLock getLock() {
		return viewerListLock;
	}

	public abstract void print();
	public abstract void endGame();
}
