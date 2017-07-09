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
		getLock().lock();
		viewers.remove(viewer);
		getLock().unlock();
	}

	protected ReentrantLock getLock() {
		return viewerListLock;
	}

	public abstract void print();
	public abstract void endGame();
	
	public void sendScores(Collection<? extends Player<?>> players) {
		ByteBuffer scoreBuffer = getPlayerScoreBuffer(players);
		
		getLock().lock();
		for(Consumer<ByteBuffer> viewer : viewers) {
			viewer.accept(scoreBuffer.duplicate());
		}
		getLock().unlock();
	}

	private ByteBuffer getPlayerScoreBuffer(Collection<? extends Player<?>> players) {
		ByteBuffer buffer = ByteBuffer.allocate(players.size() * 2 * Integer.SIZE + 2);
		buffer.put((byte) 3);
		buffer.put((byte) players.size());
		
		for(Player<?> player : players) {
			buffer.putInt(player.getID());
			buffer.putInt(player.getScore());
		}
		
		buffer.flip();
		
		return buffer;
	}
}
