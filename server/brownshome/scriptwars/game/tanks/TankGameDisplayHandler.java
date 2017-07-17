package brownshome.scriptwars.game.tanks;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import brownshome.scriptwars.game.Game;
import brownshome.scriptwars.game.GridDisplayHandler;
import brownshome.scriptwars.game.GridItem;
import brownshome.scriptwars.game.Player;

public class TankGameDisplayHandler extends GridDisplayHandler {
	private static final int PLAYER_START = 4;
	
	public static class ShotImage implements GridItem {
		private final Coordinates position, move;
		private final boolean isJustBorn;
		
		public ShotImage(Shot shot) {
			isJustBorn = shot.getPrevious().equals(shot.getOwner().getPosition());
			position = isJustBorn ? shot.getPosition() : shot.getPrevious();
			move = new Coordinates(shot.getPosition().getX() - shot.getPrevious().getX(), shot.getPosition().getY() - shot.getPrevious().getY());
		}

		@Override
		public byte getCode() {
			return isJustBorn ? (byte) 2 : (byte) 3;
		}

		@Override
		public Coordinates getMove() {
			return move;
		}

		public Coordinates getPosition() {
			return position;
		}
	}

	private static final byte PLAYER_ID_BYTE = GridDisplayHandler.FREE_ID;
	
	public TankGameDisplayHandler(Game game) {
		super(game);
	}

	@Override
	protected void updatePlayerTable(Collection<Consumer<ByteBuffer>> oldViewers) {
		super.updatePlayerTable(oldViewers);
		send(getPlayerIDBuffer(), oldViewers);
	}
	
	
	@Override
	protected void handleNewViewers(Collection<Consumer<ByteBuffer>> newViewers) {
		send(getPlayerIDBuffer(), newViewers);
		super.handleNewViewers(newViewers);
	}
	
	private ByteBuffer getPlayerIDBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES * game.getMaximumPlayers());
		buffer.put(PLAYER_ID_BYTE);
		IntBuffer intBuffer = buffer.asIntBuffer();
		
		TankGame game = (TankGame) this.game;
		
		for(Player<?> player : game.getActivePlayers()) {
			intBuffer.put(game.getIndex(player), player.getID());
		}
		
		buffer.rewind();
		return buffer;
	}
	
	@Override
	public TankGame getGame() {
		return (TankGame) super.getGame();
	}
	
	private static class WallGridItem implements GridItem {
		private static final Coordinates coord = new Coordinates(0, 0);
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof WallGridItem;
		}
		
		@Override
		public byte getCode() {
			return 1;
		}

		@Override
		public Coordinates getMove() {
			return coord;
		}

		@Override
		public int hashCode() {
			return 1;
		}
	}
	
	private class TankGridItem implements GridItem {
		private final Coordinates coord;
		private final byte code;
		
		public TankGridItem(Tank tank) {
			if(tank.hasMoved()) {
				Direction dir = tank.getDirection();
				coord = new Coordinates(dir.dx, dir.dy);
			} else {
				coord = new Coordinates(0, 0);
			}
			
			code = (byte) (((TankGame)TankGameDisplayHandler.this.game).getIndex(tank.getOwner()) + PLAYER_START);
		}
		
		@Override
		public byte getCode() {
			return code;
		}

		@Override
		public Coordinates getMove() {
			return coord;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(code, coord);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof TankGridItem
					&& ((TankGridItem) obj).code == code
					&& ((TankGridItem) obj).coord.equals(coord);
		}
	}
	
	protected void displayWorld(World world) {
		GridItem[][] display = new GridItem[world.getHeight()][world.getWidth()];
		
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				if(world.isWall(x, y))
					display[y][x] = new WallGridItem();
				else {
					Tank tank = world.getTank(x, y);
					if(tank != null)
						display[y][x] = new TankGridItem(tank);
					else
						display[y][x] = null;
				}
			}
		}
		
		for(ShotImage shot : world.getShotItemsToRender()) {
			display[shot.getPosition().getY()][shot.getPosition().getX()] = shot;
		}
		
		putGrid(display);
	}
}
