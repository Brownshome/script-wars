<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean"/>

<h2>About</h2>
<p>
Tanks is a simple game of stealth and tactics. You move around the map trying to 
find the enemy tanks and shoot them while avoiding getting shot. You are only sent 
the positions of tanks that you can see but are sent bullets at all times. This 
means that shooting will let the other tanks know where you are.
</p><p>
This game is classed as easy because there is not much data to handle and an 
effective bot can be made with less than one hundred lines of code. This does not 
mean that complex bots are not possible. There are many tricks and strategies that
a tank can use to outsmart their opponent.
</p>
<hr>

<h2>Rules</h2>
<p>
Each tick every tank can move in any direction or fire a shot
in any direction. The tank moves 1 space per turn and each shot moves 
<c:out value="${staticBean.tankGameShotSpeed}"/> spaces per turn.
</p><p>
You can see an enemy if there is a rectangle that can be drawn that contains both 
you and the other tanks that contains no walls.
</p><p>
Tanks have an ammo restraint to make trigger happy tanks less effective. Each 
tank has a maximum of ${staticBean.tankGameAmmo} ammo.
</p><p>
Ammo pickups, shown as a pile of shots on the game viewer, fully restores a tank's ammo
reserve. Furthermore, any kill will fully restore your ammo.
</p><p>
On death, you will be sent one set of data with the <code>isAlive</code> byte set to
zero. <strong>There will be no other data in this dataset so do not attempt to read
any, seriously, we are NOT hiding anything in this data.</strong>
</p><p>
One point is gained for every kill and one point is lost for every death, it is not possible to have negative points.
</p><p>
The coordinate system is defined so that (0, 0) is at the top left of the game board. So
to move UP subtract one from your y coordinate.
</p>
<hr>

<h2>On Each Game Tick</h2>
<div class="media">
	<div class="media-left"><h1>1.</h1></div>
	<div class="media-body media-middle"><p>
	The tanks are moved by each player. If two tanks try to move into the same spot
	they neither of them will move. Keep a look out for this to avoid getting your 
	tank stuck in loops.
	</p></div>
</div>
<div class="media">
	<div class="media-left"><h1>2.</h1></div>
	<div class="media-body media-middle"><p>
	Shots move X spaces. Any tanks on spaces that the shot moves into will be 
	destroyed and lose a point while the tank than shot the shot will gain a point. 
	Shots cannot pass through each other and cannot share the same space. If this does
	occur all colliding shots will be destroyed.
	</p></div>
</div>
<div class="media">
	<div class="media-left"><h1>3.</h1></div>
	<div class="media-body media-middle"><p>
	Tanks fire shots. The shot appears in the space in-front of the tank killing 
	any tank that is in that spot instantly. All tanks fire at the same time, so
	two tanks next to each other shooting will both die instantly.
	</p></div>
</div>
<hr>

<h2>Example Situations</h2>
<div class="media"><div class="media-left">
		<img class="media-object" src="${root}/static/games/Tanks/eg1.png" alt="Example 1" style="width:128px;height:128px;border:5px solid black">
	</div><div class="media-body">
		<p>If both tanks fire towards each other they will both be killed as all tanks fire instantly. If
		one tank shoots and one tank moves up no tank will die as the tanks are moved each tick before shots
		are fired.</p>
</div></div>

<div class="media"><div class="media-left">
		<img class="media-object" src="${root}/static/games/Tanks/eg2.png" alt="Example 2" style="width:128px;height:128px;border:5px solid black">
	</div><div class="media-body">
		<p>The bullet is moving upwards. If the tank moves onto the bullet it will not be killed
		and the bullet will continue on its way. This is because tanks are only killed if a bullet moves
		onto the space that they occupy.</p>
</div></div>

<div class="media"><div class="media-left">
		<img class="media-object" src="${root}/static/games/Tanks/eg3.png" alt="Example 3" style="width:128px;height:128px;border:5px solid black">
	</div><div class="media-body">
		<p>Both tanks try to move into the central space at the same time. They are both cannot move as two tanks cannot
		occupy the same space. Neither tank can see the other one so make sure to check if your moves succeed to avoid
		getting stuck.</p>
</div></div>

<div class="media"><div class="media-left">
		<img class="media-object" src="${root}/static/games/Tanks/eg4.png" alt="Example 4" style="width:128px;height:128px;border:5px solid black">
	</div><div class="media-body">
		<p>The bullet is moving upwards. If the tank moves to the side it will not be killed as tanks are moved before shots
		are moved in each game tick.</p>
</div></div>
<hr>

<h2>Data protocol</h2>
<p>There are two different ways to connect to the game. The first is to use the custom
made Java API kindly written by <a href="https://github.com/liamtbrand">Liam</a> to connect to the game. 
The second is to use the Network class to connect to the game, this is the lower level API that
all games use and allows access to the raw data sent from the server. The first option is more 
recommended for general use, but if you are using a language other than Java or know 
what you are doing the second approach may be more applicable.</p>

<div class="panel panel-default"><div class="panel-body">
<ul class="nav nav-tabs">
	<li class="active">
		<a href="#custom" data-toggle="tab">
			<strong>High Level / Java</strong>
		</a>
	</li>
	<li>
		<a href="#basic" data-toggle="tab">
			<strong>Low Level / Cross Code</strong>
		</a>
	</li>
</ul>

<div class="tab-content">
	<div class="tab-pane active" id="custom">
		<h3>High Level API</h3>
	
		<p>This is an API written by <a href="https://github.com/liamtbrand">Liam</a> to 
		make the creation of Tank bots quicker. It handles the reading of the data 
		and several common tasks for you to allow you to concentrate more on the 
		more important things such as <strong>beating your opponents into submission.</strong></p>
		
		<p>There are several classes that you will need to pay attention to in order to use
		this API:</p>
		
		<p>The <code>brownshome.scriptwars.game.tanks.Direction</code> class contains constants
		for each direction and methods for manipulating coordinates and directions.
		<br>The <code>brownshome.scriptwars.game.tanks.Shot</code> class describes a shot on the game
		grid. 
		<br>The <code>brownshome.scriptwars.game.tanks.Tank</code> class describes a tank in the game
		world. 
		<br>The <code>brownshome.scriptwars.game.tanks.World</code> class contains most of the methods that
		can be used to interact with the map, such as the position of ammo pickups, shots and walls.
		<br>The <code>brownshome.scriptwars.game.tanks.Action</code> class contains constants for
		all the actions a tank can make. 
		<br>The <code>brownshome.scriptwars.game.tanks.Coordinates</code>
		class is an immutable (it cannot be edited) object containing the methods <code>int getX()</code>
		and <code>int getY()</code> that represents a position on the game world. 
		<br>Finally, the <code>brownshome.scriptwars.game.tanks.TankAPI</code> contains all the functions needed to interact
		with the game server.</p>
		
		<p>First call the constructor <code>TankAPI(int id, String ip, String name)
		</code> to create a <code>TankAPI</code> object to communicate with. At the start
		of each loop call <code>nextTick()</code>. This method gets data from the server and sends any
		actions you have set, it returns a boolean which will be false if the connection has failed for
		some reason. The get data from the server using the functions in the API and use it to decide what
		to do. Some useful functions are <code>getVisibleShots()</code>, <code>getVisibleTanks()</code>,
		<code>getMap().isWall(Coordinates coordinate)</code> and <code>getMap().getTank(Coordinates coordinate)</code>. There
		are other functions that may be useful; a link to the documentation is provided at the bottom of this section.
		<br>
		Once you have decided what action to take call either <code>move(Direction direction)</code>, 
		<code>shoot(Direction direction)</code> or <code>doNothing()</code>. This will set what action
		your tank will do when <code>nextTick()</code> is called.<p>
		
		<p class="text-center"><a class="btn btn-primary btn-lg" href="/doc/brownshome/scriptwars/game/tanks/TankAPI.html">Documentation</a></p>
		
		<h3>Example Code</h3>
		<p>This is a basic AI showing how to use the classes. This AI will move randomly while shooting at 
		any enemies it can see. The command to compile this AI is <code>javac -cp &quot;script-wars-client.jar&quot; 
		ExampleTankAI.java</code> and the command to run this AI is <code>java -cp &quot;.;script-wars-client.jar&quot;
		ExampleTankAI INSERT-ID-HERE</code></p>
		
<pre><code>
import java.io.IOException;
import brownshome.scriptwars.game.tanks.*;
public class ExampleTankAI {	/**	 * The main method to start the AI and connect to the server.	 * 	 * args[0] should contain the game id.	 * You can request one from: http://script-wars.com/games/Tanks	 * by clicking the 'Join' button.	 * 	 * @param args The input arguments containing the ID allocated by the server	 * @throws IOException If we failed to connect to the server	 */	public static void main(String[] args) throws IOException {		// args[0] should contain the game id.		// You can request one from: http://script-wars.com/games/Tanks		// by clicking the 'Join' button				int id;		if(args.length &gt; 0){			id = Integer.valueOf(args[0]);		} else {			System.out.println(&quot;Usage: JAVACOMMAND serverid&quot;);			System.exit(1);			return;		}
		TankAPI api = new TankAPI(id, &quot;www.script-wars.com&quot;, &quot;Example AI 1&quot;);
		while(api.nextTick()) {
			if(!api.isAlive()) {
				continue;
			}			// Move randomly, this will be overwritten if we can see someone.			int direction = (int) (Math.random() * 4);			api.move(Direction.values()[direction]);
				// See if there is a tank in our field of view,			// and if there is select it.			Tank targetTank = null;			for(Tank tank : api.getVisibleTanks()){				targetTank = tank;			}				// If we can see a tank, lets shoot it.			if(targetTank != null){				Coordinates targetPosition = targetTank.getPosition();				Coordinates myPosition = api.getCurrentPosition();						Direction targetDirection = Direction.getDirection(targetPosition, myPosition);				if(targetDirection != null) {					//We have a clear shot on the target					api.shoot(targetDirection);				}			}				System.out.println(&quot;Position: &quot; + api.getCurrentPosition());		}				System.out.println(&quot;Disconnected from server:\n\t&quot; + api.getConnectionStatus());	}}
</code></pre>
	</div>
	<div class="tab-pane" id="basic">
		<h3>Low Level API</h3>
		<p>This is a simpler API that is used internally by all games and all
		high level APIs. Due to it's simplicity it is sometimes the only API
		that can be used if the game is new or you are not writing your AI in Java.</p>
		
		<p>To use this API you create a <code>brownshome.scriptwars.connection.Network</code> object using the <code>Network(int ID, String ip, String name)</code>
		constructor. Then, in your loop, call <code>nextTick()</code> to get a new set of data from the server and send queued data.
		This function will return false if there is a problem with the connection. Then extract data from the server using
		this <code>getX()</code> family of functions, where X is the data type you want. And send data using the <code>sendX()</code> family
		of functions.</p>
		
		<p class="text-center"><a class="btn btn-primary btn-lg" href="${root}/doc/brownshome/scriptwars/connection/Network.html">Documentation</a></p>
		
		<p>For the tank game in particular the data received from the server is laid out in the table below.</p>
		<table class="table">
			<tr>
				<th>Name</th>
				<th>Amount</th>
				<th>Type</th>
				<th>Meaning</th>
			</tr>
			<tr>
				<td>isAlive</td>
				<td>1</td>
				<td>Byte</td>
				<td>0 if the player is dead, 1 if the player is dead. There will be no more data to follow if this value is 0.</td>
			</tr>
			<tr>
				<td>ammoRemaining</td>
				<td>1</td>
				<td>Byte</td>
				<td>The amount of ammunition you have remaining.</td>
			</tr>
			<tr>
				<td>xPos</td>
				<td>1</td>
				<td>Byte</td>
				<td>The x coordinate of the player.</td>
			</tr>
			<tr>
				<td>yPos</td>
				<td>1</td>
				<td>Byte</td>
				<td>The y coordinate of the player.</td>
			</tr>
			<tr>
				<td>gridWidth</td>
				<td>1</td>
				<td>Byte</td>
				<td>The width of the game grid.</td>
			</tr>
			<tr>
				<td>gridHeight</td>
				<td>1</td>
				<td>Byte</td>
				<td>The height of the game grid.</td>
			</tr>
			<tr>
				<td>isWall</td>
				<td>gridWidth * gridHeight</td>
				<td>Boolean</td>
				<td>true if there is a wall, false otherwise. The values are from top to bottom, left to right, row by row.
				e.g. (0, 0) to (gridWidth - 1, 0) then (1, 0) ...</td>
			</tr>
			<tr>
				<td>numberOfTanks</td>
				<td>1</td>
				<td>Byte</td>
				<td>The number of tanks that can be seen by the player.</td>
			</tr>
			<tr>
				<td>tankData</td>
				<td>numberOfTanks</td>
				<td>(Byte, Byte, Byte)</td>
				<td>The x and y coordinates and a unique ID of a tank that can be seen. 
				No guarantees are made about the order of the tanks. If a tank disconnects it's ID may be re-used</td>
			</tr>
			<tr>
				<td>numberOfShots</td>
				<td>1</td>
				<td>Byte</td>
				<td>The number of shots in the game</td>
			</tr>
			<tr>
				<td>shotData</td>
				<td>numberOfShots</td>
				<td>(Byte, Byte, Byte)</td>
				<td>The x and y coordinates followed by the direction of the shot. The values for
				specific directions are shown below.</td>
			</tr>
			<tr>
				<td>numberOfAmmoPickups</td>
				<td>1</td>
				<td>Byte</td>
				<td>The number of ammo pickups in the game</td>
			</tr>
			<tr>
				<td>ammoPickupData</td>
				<td>numberOfAmmoPickups</td>
				<td>(Byte, Byte)</td>
				<td>The x and y coordinates of the pickup</td>
			</tr>
		</table>
		
		<p>Each turn two bytes must be sent to the server.</p>
		<table class="table">
			<tr>
				<th>Name</th>
				<th>Amount</th>
				<th>Type</th>
				<th>Meaning</th>
			</tr>
			<tr>
				<td>action</td>
				<td>1</td>
				<td>Byte</td>
				<td>The value given in the action table. If the action is nothing then there doesn't need to be a direction
				byte send.</td>
			</tr>
			<tr>
				<td>direction</td>
				<td>1</td>
				<td>Byte</td>
				<td>The direction that the action is to take place in.</td>
			</tr>
		</table>
		
		<p>The values of the direction and action bytes are given below.</p>
		<div class="row"><div class="col-md-6">
		<h4>Action Byte</h4>
		<table class="table">
			<tr>
				<th>Value</th>
				<th>Meaning</th>
			</tr><tr>
				<td>0</td><td>No action (No direction required)</td>
			</tr><tr>
				<td>1</td><td>Move</td>
			</tr><tr>	
				<td>2</td><td>Shoot</td>
			</tr>
		</table>
		</div><div class="col-md-6">
		<h4>Direction Byte</h4>
		<table class="table">
			<tr>
				<th>Value</th>
				<th>Meaning</th>
			</tr><tr>
				<td>0</td><td>Up</td>
			</tr><tr>
				<td>1</td><td>Left</td>
			</tr><tr>	
				<td>2</td><td>Down</td>
			</tr><tr>	
				<td>3</td><td>Right</td>
			</tr>
		</table>
		</div></div>
		
		<h3>Example Code</h3>
		<pre><code>
import java.io.IOException;

import brownshome.scriptwars.connection.Network;

/**
 * This is an example of reading data from the server. We avoid using the pre-built Tank and
 * Shot classes as they would not be available in languages other than Java. The actual processing
 * of the data is left to the reader. But structs or classes would be useful.
 * 
 * @author James
 */
public class ExampleTankAIBasic {
	private static Network network;
	
	/**
	 * The main method to start the AI and connect to the server.
	 * 
	 * args[0] should contain the game id.
	 * You can request one from: http://script-wars.com/games/Tanks
	 * by clicking the 'Join' button.
	 * 
	 * @param args The input arguments containing the ID allocated by the server
	 * @throws IOException If we failed to connect to the server
	 */
	public static void main(String[] args) throws IOException {
		// args[0] should contain the game id.
		// You can request one from: http://script-wars.com/games/Tanks
		// by clicking the 'Join' button

		int id;
		if(args.length &gt; 0){
			id = Integer.valueOf(args[0]);
		} else {
			System.out.println(&quot;Usage: JAVACOMMAND serverid&quot;);
			System.exit(1);
			return;
		}

		network = new Network(id, &quot;www.script-wars.com&quot;, &quot;John Smith Low Level&quot;);
		int direction = 0; //The initial direction, UP
		boolean hasHitWall = false;

		while(network.nextTick()) {
			boolean isAlive = network.getByte() == 1;
			if(!isAlive) {
				network.sendByte(0);
				System.out.println(&quot;We are dead, not big surprise&quot;);
				continue;
			}
			
			int ammo = network.getByte();
			
			int xPos = network.getByte();
			int yPos = network.getByte();
			
			int width = network.getByte();
			int height = network.getByte();
			
			boolean[][] map = new boolean[width][height];
			for(int y = 0; y &lt; height; y++) {
				for(int x = 0; x &lt; width; x++) {
					map[y][x] = network.getBoolean();
				}
			}
			
			int noTanks = network.getByte();
			
			int[][] tanks = new int[noTanks][3]; //An array holding the X, Y and ID of each tank
			for(int i = 0; i &lt; noTanks; i++) {
				tanks[i][0] = network.getByte();
				tanks[i][1] = network.getByte();
				tanks[i][2] = network.getByte();
			}
			
			int noShots = network.getByte();
			
			int[][] shots = new int[noShots][3]; //An array holding the X, Y and direction of each shot
			for(int i = 0; i &lt; noShots; i++) {
				shots[i][0] = network.getByte();
				shots[i][1] = network.getByte();
				shots[i][2] = network.getByte();
			}
			
			int noAmmo = network.getByte();
			int[][] ammoPickups = new int[noAmmo][2];
			for(int i = 0; i &lt; noAmmo; i++) {
				ammoPickups[i][0] = network.getByte();
				ammoPickups[i][1] = network.getByte();
			}
			
			//For the actual AI, we shall hug the left wall and keep moving around, should keep things interesting
			//Check the anti-clockwise direction, then the straight, then the clockwise direction, then the reverse
			int antiClock = (direction + 1) % 4; //Cool right : D
			int clockwise = (direction + 3) % 4;
			int reverse = (direction + 2) % 4;
			
			if(isWall(direction, xPos, yPos, map)) {
				hasHitWall = true;
			}
			
			if(hasHitWall) {
				if(!isWall(antiClock, xPos, yPos, map)) {
					direction = antiClock;
				} else if(!isWall(direction, xPos, yPos, map)){
					//Don't change the direction
				} else if(!isWall(clockwise, xPos, yPos, map)) {
					direction = clockwise;
				} else if(!isWall(reverse, xPos, yPos, map)) {
					direction = reverse;
				} else {
					//We are trapped in a box, time to panic, and panic hard
				}
			}
			
			network.sendByte(1); //Move
			network.sendByte(direction);
		}
		
		System.out.println(network.getConnectionStatus()); //This function may not be available on some languages.
	}
	
	/**
	 * Check if there is a wall in the direction we are trying to go.
	 */
	private static boolean isWall(int direction, int xPos, int yPos, boolean[][] map) {
		switch(direction) {
			case 0: //UP
				yPos--;
				break;
			case 1: //LEFT
				xPos--;
				break;
			case 2: //DOWN
				yPos++;
				break;
			case 3: //RIGHT
				xPos++;
				break;
		}
		
		//x and yPos are now the next space.
		return map[yPos][xPos];
	}
}
		</code></pre>
	</div>
</div>
</div>
</div>