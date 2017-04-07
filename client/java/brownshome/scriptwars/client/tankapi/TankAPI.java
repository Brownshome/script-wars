package brownshome.scriptwars.client.tankapi;

import brownshome.scriptwars.client.Network;

/**
 * This is a wrapper over the Network class that gives an api to control the Tank.
 * This can be used for controlling the tank without knowing the
 * underlying network implementation.<br />
 * <br />
 * NOTE: This code is currently in beta.
 *
 * @author Liam T. Brand
 * @author James Brown
 *
 */
public class TankAPI {

	public static enum Direction {
		UP, DOWN, LEFT, RIGHT;
	}

	static final int TANK = -3;
    static final int SPACE = -2;
    static final int WALL = -1;

    static final int NOACTION = 0;
    static final int MOVE = 1;
    static final int SHOOT = 2;

	private boolean _isAlive;
	private int _x;
	private int _y;
	private int _width;
	private int _height;
	private int[][] _grid;

	private int _tanks;
	private int _tankX;
	private int _tankY;

	private int _shots;
	private int _shotX;
	private int _shotY;

	private boolean _send = false;

	private int _actionByte;
	private int _directionByte;

	public TankAPI(int id, String address, int port, String username){
		Network.connect(id, address, port, username);
	}

	private void setSendData(){
		if(_send){

			Network.sendByte(_actionByte);                    //Action Byte, in this case MOVE
            Network.sendByte(_directionByte);                    //Direction Byte, in this case DOWN

		}else{
			_send = true;
		}

		_actionByte = NOACTION; // Default action
		_directionByte = 0; // Default direction //TODO fix
	}

	public boolean nextTick(){

		setSendData();

		while(!Network.nextTick()){
			// Wait for next tick...
		}

		_isAlive = Network.getByte() == 1;          //Is the player alive
        if(_isAlive) {
            _x = Network.getByte();              //X position
            _y = Network.getByte();              //Y position
            _width = Network.getByte();          //game width
            _height = Network.getByte();         //game height

            _grid = new int[_height][_width];

            for(int row = 0; row < _grid.length; row++) {
                for(int column = 0; column < _grid[row].length; column++) {
                    if(Network.getBoolean()) {      //Is wall
                        _grid[row][column] = WALL;
                    } else {
                        _grid[row][column] = SPACE;
                    }
                }
            }

            _tanks = Network.getByte();             //Number of tanks
            for(int i = 0; i < _tanks; i++) {
                _tankX = Network.getByte();      //Tank x
                _tankY = Network.getByte();      //Tank y
                _grid[_tankY][_tankX] = TANK;
            }

            _shots = Network.getByte();          //Number of Shots
            for(int i = 0; i < _shots; i++) {
                _shotX = Network.getByte();      //Shot x
                _shotY = Network.getByte();      //Shot y
                                                    //Shot direction
                _grid[_shotX][_shotY] = Network.getByte();
            }

        } else {
            Network.sendByte(0);                    //Send stuff always, or get dropped
            System.out.println("We Are Dead");
        }

		return true;
	}

	public void move(int direction) {
		_actionByte = MOVE;
		_directionByte = direction;
	}

}
