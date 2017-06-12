package brownshome.scriptwars.game;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

public class GridDisplayHandler extends DisplayHandler {
	private char[][] grid;
	private char[][] oldGrid;
	List<Player<?>> playerList;
	
	public void setPlayerList(List<Player<?>> players) {
		assert this.playerList == null;
		playerList = players;
	}
	
	/**
	 * For send format see GameViewerSocket.
	 */
	@Override
	public void print() {
		ByteBuffer buffer = getBulkSyncBuffer();
		
		getLock().lock();
		{
			if(!newViewers.isEmpty()) {
				ByteBuffer idUpdateBuffer = getPlayerIDListBuffer();

				for(Consumer<ByteBuffer> viewer : newViewers) {
					viewer.accept(idUpdateBuffer.duplicate());
					viewer.accept(buffer.duplicate()); //send bulk update
				}
			}

			if(!shouldBulkSync()) {
				ByteBuffer delta = getDeltaBuffer();
				if(delta.remaining() < buffer.remaining())
					buffer = delta;
			}

			for(Consumer<ByteBuffer> viewer : viewers) {
				viewer.accept(buffer.duplicate()); //duplicating for thread safety and async uploads
			}

			viewers.addAll(newViewers);
			newViewers.clear();
		}
		getLock().unlock();
		
		if(oldGrid == null)
			oldGrid = new char[getHeight()][getWidth()];
		
		for(int row = 0; row < grid.length; row++) {
			System.arraycopy(grid[row], 0, oldGrid[row], 0, grid[row].length);
		}
	}

	private ByteBuffer getPlayerIDListBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Byte.BYTES + Integer.BYTES * playerList.size());
		buffer.put((byte) 4);
		buffer.put((byte) playerList.size());
		
		for(Player<?> player : playerList) {
			buffer.putInt(player == null ? 0 : player.getID());
		}
		buffer.flip();
		
		return buffer;
	}

	/** Sends the player list to the viewers, null players are sent as a zero. */
	public synchronized void sendPlayerIDs() {
		ByteBuffer buffer = getPlayerIDListBuffer();
		
		for(Consumer<ByteBuffer> viewer : viewers) {
			viewer.accept(buffer);
		}
	}

	private boolean shouldBulkSync() {
		if(oldGrid == null || grid.length != oldGrid.length || grid[0].length != oldGrid[0].length) {
			return true;
		}
		
		return false;
	}

	private ByteBuffer getBulkSyncBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Byte.BYTES + Byte.BYTES + Character.BYTES * getWidth() * getHeight());
		
		buffer.put((byte) 0);
		buffer.put((byte) getWidth()).put((byte) getHeight());
		
		for(char[] row : grid) {
			for(char c : row) {
				buffer.putChar(c);
			}
		}
		
		buffer.flip();
		return buffer;
	}

	private ByteBuffer getDeltaBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + (Character.BYTES + Byte.BYTES + Byte.BYTES) * getWidth() * getHeight());
	
		buffer.put((byte) 1);
		
		for(int y = 0; y < grid.length; y++) {
			for(int x = 0; x < grid[y].length; x++) {
				if(oldGrid[y][x] != grid[y][x]) {
					buffer.putChar(grid[y][x]).put((byte) x).put((byte) y);
				}
			}
		}
		
		buffer.flip();
		
		return buffer;
	}

	public int getHeight() {
		return grid.length;
	}

	public int getWidth() {
		return grid[0].length;
	}

	public void putGrid(char[][] grid) {
		this.grid = grid;
	}

	/**
	 * @param width
	 * @param height
	 * @param x The position of the upper left corner
	 * @param y The position of the upper left corner
	 * @param character
	 */
	public void putSquare(float width, float height, float x, float y, char character) {
		for(int yCoord = (int) (grid.length * y); yCoord >= 0 && yCoord < grid.length; yCoord++) {
			for(int xCoord = (int) (grid[yCoord].length * x); xCoord >= 0 && xCoord < grid[yCoord].length; xCoord++) {
				grid[yCoord][xCoord] = character;
			}
		}
	}
}
