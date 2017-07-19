package brownshome.scriptwars.game;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;

/** A DisplayHandler that displays a grid of static items and dynamic items */
public class GridDisplayHandler extends DisplayHandler {
	private static final byte STATIC_UPDATE_BYTE = DisplayHandler.FREE_ID;
	private static final byte DYNAMIC_UPDATE_BYTE = DisplayHandler.FREE_ID + 1;
	protected static final byte FREE_ID = DisplayHandler.FREE_ID + 2;
	
	private byte[][] staticGrid;
	private boolean isStaticGridDirty = true;
	private Collection<GridItem> dynamicItems = Collections.emptyList();
	
	public GridDisplayHandler(Game game) {
		super(game);
	}
	
	@Override
	protected void handleNewViewers(Collection<Consumer<ByteBuffer>> newViewers) {		
		super.handleNewViewers(newViewers);
		
		if(staticGrid == null)
			return;
		
		send(getStaticBuffer(), newViewers);
	}
	
	@Override
	public void handleOldViewers(Collection<Consumer<ByteBuffer>> viewers) {
		super.handleOldViewers(viewers);
		
		if(staticGrid == null)
			return;
		
		if(isStaticGridDirty) {
			ByteBuffer buffer = getStaticBuffer();
			send(buffer, viewers);
			isStaticGridDirty = false;
		}
		
		ByteBuffer buffer = getDeltaBuffer();

		if(buffer.hasRemaining()) 
			send(buffer, viewers);
		
		dynamicItems.clear();
	}

	public synchronized void setDynamicItems(Collection<GridItem> items) {
		dynamicItems = items;
	}
	
	protected ByteBuffer getStaticBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(3 + getWidth() * getHeight());
		
		buffer.put(STATIC_UPDATE_BYTE);
		buffer.put((byte) getWidth()).put((byte) getHeight());
		
		for(byte[] row : staticGrid) {
			for(byte c : row) {
				buffer.put(c);
			}
		}
		
		buffer.flip();
		return buffer;
	}

	protected ByteBuffer getDeltaBuffer() {
		if(dynamicItems == null || dynamicItems.isEmpty())
			return ByteBuffer.allocate(0);
		
		//code, sx, sy, ex, ey
		ByteBuffer buffer = ByteBuffer.allocate(1 + 5 * getWidth() * getHeight());
	
		buffer.put(DYNAMIC_UPDATE_BYTE);
		
		for(GridItem item : dynamicItems) {
			buffer.put(item.code());
			buffer.put((byte) item.start().getX()).put((byte) item.start().getY());
			buffer.put((byte) item.end().getX()).put((byte) item.end().getY());
		}
		
		buffer.flip();
		
		return buffer;
	}

	public int getHeight() {
		return staticGrid.length;
	}

	public int getWidth() {
		return staticGrid[0].length;
	}

	public synchronized void putStaticGrid(byte[][] grid) {
		isStaticGridDirty = true;
		staticGrid = grid;
	}

	public Game getGame() {
		return game;
	}
}