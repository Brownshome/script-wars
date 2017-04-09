package brownshome.scriptwars.client.tankapi;

import java.util.ArrayList;
import java.util.List;

import brownshome.scriptwars.client.Network;
import brownshome.scriptwars.server.game.tanks.*;

/**
 * This is a wrapper over the Network class that gives an API to control the Tank.
 * This can be used for controlling the tank without knowing the
 * underlying network implementation.
 * <br>
 * NOTE: This code is currently in beta.
 *
 * @author Liam T. Brand
 * @author James Brown
 *
 */
public class TankAPI {
	public enum ConnectionStatus{
		CONNECTED, DROPPED;
	}

	public class Tank {
		private Coordinates _coord;

		public Tank(Coordinates c){
			_coord = c;
		}

		public Coordinates getCoordinates(){
			return _coord;
		}
	}

	public class Shot {
		private Coordinates _coord;
		private Direction _direction;

		public Shot(Coordinates c, Direction d){
			_coord = c;
			_direction = d;
		}

		public Coordinates getCoordinates(){
			return _coord;
		}
		
		public Direction getDirection(){
			return _direction;
		}
	}
	
	public class Map {
		
		public static final boolean SPACE = false;
		public static final boolean WALL = true;
		
		private final boolean[][] _walls;
		
		protected Map(final boolean[][] walls){
			_walls = walls;
		}
		
		public boolean isWall(int x, int y){
			return _walls[y][x];
		}
		
		public boolean isWall(Coordinates c){
			return isWall(c.getX(),c.getY());
		}
		
		public int getHeight(){
			return _walls.length;
		}
		
		public int getWidth(){
			return _walls[0].length;
		}
	}

	private Map _map;
	
	private boolean _isAlive;
	private Tank _me;
	private List<Tank> _tanks;
	private List<Shot> _shots;
	
	private int _actionByte;
	private int _directionByte;

	private boolean _firstSend;
	
	private ConnectionStatus _connStatus;

	public TankAPI(int id, String address, String username){
		Network.connect(id, address, username);
		_firstSend = true;
		_connStatus = ConnectionStatus.CONNECTED;
	}

	/**
	 * This is called at the end of each tick.
	 * This will cause data for the last tick to be sent to the server.
	 */
	private void setSendData(){
		if(!_firstSend){

			Network.sendByte(_actionByte);                    //Action Byte, in this case MOVE
			Network.sendByte(_directionByte);                    //Direction Byte, in this case DOWN

		}else{
			_firstSend = false;
		}

		_actionByte = Action.NOTHING.ordinal(); // Default action
		_directionByte = Direction.UP.ordinal(); // Default direction //TODO fix
	}

	/**
	 * This is the main API loop.
	 * This should be called from a while loop inside which is
	 * the main code of the AI. See the example AI.
	 * @return True when we have entered into the next game tick.
	 */
	public ConnectionStatus nextTick(){

		setSendData();

		if(Network.nextTick()){
			// Wait for next tick...
		}else{
			_connStatus = ConnectionStatus.DROPPED;
		}

		_isAlive = Network.getByte() == 1;          // Is the player alive
		if(_isAlive) {
			int x = Network.getByte();              // X position
			int y = Network.getByte();              // Y position
			
			_me = new Tank(new Coordinates(x,y));
			
			int width = Network.getByte();          // game width
			int height = Network.getByte();         // game height

			boolean[][] walls = new boolean[height][width];

			for(int row = 0; row < walls.length; row++) {
				for(int column = 0; column < walls[row].length; column++) {
					if(Network.getBoolean()) {      //Is wall
						walls[row][column] = Map.WALL;
					} else {
						walls[row][column] = Map.SPACE;
					}
				}
			}
			
			_map = new Map(walls);

			int tanks = Network.getByte();          //Number of tanks
			_tanks = new ArrayList<Tank>();
			for(int i = 0; i < tanks; i++) {
				int tankX = Network.getByte();      //Tank x
				int tankY = Network.getByte();      //Tank y
				_tanks.add(new Tank(new Coordinates(tankX,tankY)));
			}

			int shots = Network.getByte();          //Number of Shots
			_shots = new ArrayList<Shot>();
			for(int i = 0; i < shots; i++) {
				int shotX = Network.getByte();      //Shot x
				int shotY = Network.getByte();      //Shot y
				//Shot direction
				_shots.add(new Shot(new Coordinates(shotX,shotY),Direction.values()[Network.getByte()]));
			}

		} else {
			Network.sendByte(0);                    //Send stuff always, or get dropped
			System.out.println("We Are Dead");
		}

		return _connStatus;
	}

	/**
	 * Move the tank in a direction.
	 * @param direction
	 */
	public void move(Direction direction) {
		_actionByte = Action.MOVE.ordinal();
		_directionByte = direction.ordinal();
	}
	
	/**
	 * Shoots a bullet in the specified direction.
	 * @param direction
	 */
	public void shoot(Direction direction) {
		_actionByte = Action.SHOOT.ordinal();
		_directionByte = direction.ordinal();
	}
	
	/**
	 * Sets the tank to do nothing on the tick.
	 */
	public void doNothing() {
		_actionByte = Action.NOTHING.ordinal();
	}
	
	public List<Tank> getVisibleTanks(){
		return _tanks;
	}
	
	public List<Shot> getVisibleShots(){
		return _shots;
	}
	
	/**
	 * Returns the tank object for the current player.
	 * @return
	 */
	public Tank me(){
		return _me;
	}
	
	/**
	 * Returns a GameMap object.
	 * Raw data can be extracted from here for path finding etc.
	 * @return
	 */
	public Map getMap(){
		return _map;
	}	
}
