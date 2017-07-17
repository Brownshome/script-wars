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

	protected ByteBuffer getDeltaBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + (Character.BYTES + Byte.BYTES + Byte.BYTES) * getWidth() * getHeight());
	
		buffer.put(DELTA_UPDATE_BYTE);
		
		for(int y = 0; y < grid.length; y++) {
			for(int x = 0; x < grid[y].length; x++) {
				if(oldGrid[y][x] != grid[y][x]) {
					if(grid[y][x] == null) {
						buffer.put((byte) 0).put((byte) x).put((byte) y).put((byte) 0).put((byte) 0);
					} else {
						buffer.put(grid[y][x].getCode())
						.put((byte) x).put((byte) y)
						.put((byte) grid[y][x].getMove().getX()).put((byte) grid[y][x].getMove().getY());
					}
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

	public synchronized void putGrid(GridItem[][] grid) {
		if(this.grid == null || grid.length != this.grid.length || grid[0].length != this.grid[0].length)
			oldGrid = null;
		
		this.grid = grid;
	}

	public Game getGame() {
		return game;
	}
}