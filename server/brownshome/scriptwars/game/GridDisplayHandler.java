package brownshome.scriptwars.game;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.Consumer;

public class GridDisplayHandler extends DisplayHandler {
	private static final byte BULK_UPDATE_BYTE = DisplayHandler.FREE_ID;
	private static final byte DELTA_UPDATE_BYTE = DisplayHandler.FREE_ID + 1;
	protected static final byte FREE_ID = DisplayHandler.FREE_ID + 2;
	
	private GridItem[][] grid;
	private GridItem[][] oldGrid;
	
	public GridDisplayHandler(Game game) {
		super(game);
	}
	
	@Override
	protected void handleNewViewers(Collection<Consumer<ByteBuffer>> newViewers) {		
		if(grid == null)
			return;
		
		send(getBulkSyncBuffer(), newViewers);
	}
	
	@Override
	public void handleOldViewers(Collection<Consumer<ByteBuffer>> viewers) {
		super.handleOldViewers(viewers);
		
		if(grid == null)
			return;
		
		if(oldGrid == null) {
			//If there is a new grid, send a bulk update
			ByteBuffer buffer = getBulkSyncBuffer();
			oldGrid = new GridItem[getHeight()][getWidth()];
			
			send(buffer, viewers);
		} else {
			ByteBuffer buffer = getDeltaBuffer();

			if(buffer.remaining() == 0)
				return;

			send(buffer, viewers);

			for(int row = 0; row < grid.length; row++) {
				System.arraycopy(grid[row], 0, oldGrid[row], 0, grid[row].length);
			}
		}
	}

	protected ByteBuffer getBulkSyncBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Byte.BYTES + Byte.BYTES + Byte.BYTES * getWidth() * getHeight());
		
		buffer.put(BULK_UPDATE_BYTE);
		buffer.put((byte) getWidth()).put((byte) getHeight());
		
		for(GridItem[] row : grid) {
			for(GridItem c : row) {
				buffer.put(c == null ? 0 : c.getCode());
			}
		}
		
		buffer.flip();
		return buffer;
	}

	private ByteBuffer getDeltaBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + (Character.BYTES + Byte.BYTES + Byte.BYTES) * getWidth() * getHeight());
	
		buffer.put(DELTA_UPDATE_BYTE);
		
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

	@Override
	public void addViewer(Consumer<ByteBuffer> viewer) {
		if(gameHasEnded) {
			viewer.accept(getEndGameBuffer());
			return;
		}
		
		super.addViewer(viewer);
	}
	
	private ByteBuffer getEndGameBuffer() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] {DISCONNECT_BYTE});
		return buffer;
	}
	
	@Override
	public void endGame() {
		gameHasEnded = true;
		ByteBuffer buffer = getEndGameBuffer();
		
		getLock().lock();
		for(Consumer<ByteBuffer> viewer : viewers) {
			viewer.accept(buffer.duplicate());
		}
		getLock().unlock();
	}
}
