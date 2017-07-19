package brownshome.scriptwars.game;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public abstract class DisplayHandler {
	private static final byte UPDATE_GAME_TABLE = 0;
	private static final byte UPDATE_PLAYER_TABLE = 1;
	private static final byte DISCONNECT = 2;
	private static final byte UPDATE_SCORE = 3;

	protected static final byte FREE_ID = 4;
	
	private static final Collection<Consumer<ByteBuffer>> allViewers = new HashSet<>();
	private static final ReentrantReadWriteLock allViewersLock = new ReentrantReadWriteLock();
	
	public final Game game;
	
	private final Collection<Consumer<ByteBuffer>> viewers = new HashSet<>();
	private final Collection<Consumer<ByteBuffer>> newViewers = new HashSet<>();
	
	public static void sendGameTableUpdate() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] {UPDATE_GAME_TABLE});
		
		try {
			allViewersLock.readLock().lock();
			send(buffer, allViewers);
		} finally {
			allViewersLock.readLock().unlock();
		}
	}
	
	public static void removeGlobalViewer(Consumer<ByteBuffer> viewer) {
		try {
			allViewersLock.writeLock().lock();
			allViewers.remove(viewer);
		} finally {
			allViewersLock.writeLock().unlock();
		}
	}
	
	public static void addGlobalViewer(Consumer<ByteBuffer> viewer) {
		try {
			allViewersLock.writeLock().lock();
			allViewers.add(viewer);
		} finally {
			allViewersLock.writeLock().unlock();
		}
	}
	
	public DisplayHandler(Game game) {
		this.game = game;
	}
	
	/** Returns true if the viewer was added */
	public synchronized void addViewer(Consumer<ByteBuffer> viewer) {
		if(game.hasEnded()) {
			viewer.accept(ByteBuffer.wrap(new byte[] {DISCONNECT}));
			return;
		}

		newViewers.add(viewer);
		viewers.add(viewer);
	}

	public synchronized void sendUpdates() {
		if(!newViewers.isEmpty())
			handleNewViewers(newViewers);
		
		newViewers.clear();
		
		handleOldViewers(viewers);
	}
	
	protected void handleNewViewers(Collection<Consumer<ByteBuffer>> newViewers) {}
	
	protected void handleOldViewers(Collection<Consumer<ByteBuffer>> oldViewers) {
		if(game.clearScoreFlag()) {
			updateScores(oldViewers);
		} else if(game.clearPlayersFlag()) {
			updatePlayerTable(oldViewers);
		}
	}
	
	protected void updatePlayerTable(Collection<Consumer<ByteBuffer>> oldViewers) {
		ByteBuffer playerTable = ByteBuffer.wrap(new byte[] {UPDATE_PLAYER_TABLE});
	
		send(playerTable, oldViewers);
	}

	protected static void send(ByteBuffer buffer, Collection<Consumer<ByteBuffer>> viewers) {
		for(Consumer<ByteBuffer> viewer : viewers) {
			buffer.rewind();
			viewer.accept(buffer);
		}
	}

	protected void updateScores(Collection<Consumer<ByteBuffer>> oldViewers) {
		ByteBuffer scoreBuffer = getPlayerScoreBuffer(game.getActivePlayers());
		
		send(scoreBuffer, oldViewers);
	}
	
	public synchronized void removeViewer(Consumer<ByteBuffer> viewer) {
		if(game.hasEnded())
			return;
		
		viewers.remove(viewer);
		newViewers.remove(viewer);
	}

	public synchronized void endGame() {
		ByteBuffer endGameBuffer = ByteBuffer.wrap(new byte[] {DISCONNECT});
		
		send(endGameBuffer, viewers);
	}

	private ByteBuffer getPlayerScoreBuffer(Collection<? extends Player<?>> players) {
		ByteBuffer buffer = ByteBuffer.allocate(players.size() * 2 * Integer.SIZE + 2);
		buffer.put(UPDATE_SCORE);
		
		for(Player<?> player : players) {
			buffer.putInt(player.getID());
			buffer.putInt(player.getScore());
		}
		
		buffer.flip();
		
		return buffer;
	}
}
