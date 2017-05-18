package brownshome.scriptwars.client.tanks;

import java.io.IOException;
import java.util.*;

import brownshome.scriptwars.client.*;
import brownshome.scriptwars.game.tanks.*;

/**
 * This is a wrapper over the Network class that gives an API to control the Tank.
 * 
 * This can be used for controlling the tank without having to access the underlying
 * Network class.
 *
 * @author Liam T. Brand
 * @author James Brown
 *
 */
public class TankAPI {
	private World _map;
	private Network _network;
	private boolean _isAlive;
	private Tank _me;
	
	private Action _action = Action.NOTHING;
	private Direction _direction = null;
	
	/**
	 * Call this using the ID given to you by the website to connect
	 * @param id The ID given to you by the website
	 * @param address The ip of the website
	 * @param username The name of your bot
	 * @throws IOException If for some reason the site cannot be reached
	 */
	public TankAPI(int id, String address, String username) throws IOException{
		_network = new Network(id, address, username);
	}

	/**
	 * This is called at the end of each tick.
	 * This will cause data for the last tick to be sent to the server.
	 */
	private void setSendData(){
		_network.sendByte(_action.ordinal());
		if(_action != Action.NOTHING) {
			_network.sendByte(_direction.ordinal());
		}

		_action = Action.NOTHING; // Default action
		_direction = null;
	}

	/**
	 * Checks if we are alive.
	 * When this is false all other functions other than
	 * {@link #nextTick()} will have undefined results, usually null pointer exceptions. 
	 * Use this to avoid unwanted behaviour when getting data.
	 * @return true if we are alive. 
	 */
	public boolean isAlive() {
		return _isAlive;
	}
	
	/**
	 * This is the main API loop.
	 * This should be called from a while loop inside which is
	 * the main code of the AI. See the example AI.
	 * @return False if the client was disconnected or timed out. The exact cause can be found by calling
	 *         {@link #getConnectionStatus}.
	 */
	public boolean nextTick(){
		setSendData();
		
		if(!_network.nextTick()) {
			return false;
		}

		_isAlive = _network.getByte() == 1;
		if(_isAlive) {
			_me = new Tank(_network);
			_map = new World(_network);
		} else {
			_me = null;
			_map = null;
		}

		return true;
	}

	/**
	 * Move the tank in a direction.
	 * @param direction
	 */
	public void move(Direction direction) {
		assert direction != null;
		
		_action = Action.MOVE;
		_direction = direction;
	}
	
	/**
	 * Gets the relevant direction for the action next tick.
	 * If the next action is {@link brownshome.scriptwars.game.tanks.Action#NOTHING} then null will be returned.
	 * @return The direction of the next action, or null if not applicable.
	 */
	public Direction getDirection() {
		return _direction;
	}
	
	/**
	 * Gets the action that will be taken next tick
	 * @return The action that will be taken next tick
	 */
	public Action getAction() {
		return _action;
	}
	
	/**
	 * Shoots a bullet in the specified direction.
	 * @param direction
	 */
	public void shoot(Direction direction) {
		assert direction != null;
		
		_action = Action.SHOOT;
		_direction = direction;
	}
	
	/**
	 * Sets the tank to do nothing on the tick.
	 */
	public void doNothing() {
		_action = Action.NOTHING;
		_direction = null;
	}
	
	/**
	 * Returns the collection of tanks that are visible. This collection should not be edited and might be
	 * invalidated on the next tick.
	 * @return A collection of tanks.
	 */
	public Collection<Tank> getVisibleTanks(){
		return _map.getTanks();
	}
	
	/**
	 * Returns the collection of shots that are visible. This collection should not be edited and might be
	 * invalidated on the next tick.
	 * @return A collection of shots.
	 */
	public Collection<Shot> getVisibleShots(){
		return _map.getShots();
	}
	
	/**
	 * Returns the tank object for the current player.
	 * @return
	 */
	public Tank me(){
		return _me;
	}
	
	/**
	 * Returns a World object.
	 * Raw data can be extracted from here for path finding etc.
	 * <br>
	 * NB: This returns {@code null} if we are dead.
	 * 
	 * @return The world map, or {@code null} if we are dead.
	 */
	public World getMap(){
		return _map;
	}

	/**
	 * Queries the connection status. Anything other than {@link ConnectionStatus#CONNECTED}
	 * means that we are not connected to the server.
	 * 
	 * @return The connection status.
	 */
	public ConnectionStatus getConnectionStatus() {
		return _network.getConnectionStatus();
	}

	/**
	 * Prints the action to be taken next tick
	 */
	public void printAction() {
		System.out.println(_action + (_action == Action.NOTHING ? "" : " " + _direction));
	}	
}
