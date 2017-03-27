<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean"/>

<h2>Rules</h2>
<h3>Gameplay</h3>
<p>
Tank game is a 2D tactical stealth game where each player controls a tank that
attempts to shoot and destroy their enemies.
</p><p>
Each tick every tank can move in any of the cardinal directions or fire a shot
in any direction. The tank moves 1 space per turn and each shot moves 
<c:out value="${staticBean.tankGameShotSpeed}"/> spaces per turn. 
</p><p>
Each tick tanks move, then shots are moved, then shots are fired. It is not 
possible to die by moving onto a shot.
</p><p>
Players are only sent if there is straight, grid alligned line from their tank
to your tank.
</p>
<h3>Data protocol</h3>
Each tick the following data is sent:
<ul>
	<li>A byte indicating if the player is alive. A value of 1 indicates alive
	and a value of 0 indicates that the player is dead. If the player is dead
	the following data is ommitted.</li>
	<li>Two bytes containing the position of the player in the format (x, y)</li>
	<li>Two bytes containing the width and then the height of the game grid</li>
	<li>X * Y booleans containing the game world. Sent row by row with the first
	boolean being the top left corner and the last boolean being the bottom right
	corner. A value of true means that there is a wall in that grid cell.</li>
	<li>A single byte containing the number of visible players followed by an 
	x byte and a y byte for each player.</li>
	<li>A single byte  containing the number of shots on the grid followed by
	an x, a y and an byte representing a direction for each shot.</li>
</ul>
The response is expected to be 2 bytes. The first byte is the action and the second
byte is the direction value.
<table>
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
The direction values are as follows
<table>
	<tr>
		<th>Value</th>
		<th>Meaning</th>
	</tr><tr>
		<td>0</td><td>UP</td>
	</tr><tr>
		<td>1</td><td>DOWN</td>
	</tr><tr>
		<td>2</td><td>LEFT</td>
	</tr><tr>
		<td>3</td><td>RIGHT</td>
	</tr>
</table>
<h3>Example Script</h3>
<p>This is an example AI script with the code for reading data from the server filled in. It has
the AI logic missing. Feel free to use your own method of reading the data if you see fit.</p>
<pre>
import brownshome.scriptwars.client.Network;

public class AI {
	static final int TANK = -3;
	static final int SPACE = -2;
	static final int WALL = -1;
	static final int UP = 0;
	static final int DOWN = 1;
	static final int LEFT = 2;
	static final int RIGHT = 3;
	
	public static void main(String[] args) {
		Network.connect(Integer.parseInt(args[0]), &quot;13.55.154.170&quot;, 35565, &quot;John Smith&quot;);
		
		loop:
		while(Network.nextTick()) {
			boolean isAlive = Network.getByte() == 1;
			if(isAlive) {
				int x = Network.getByte();
				int y = Network.getByte();
				int width = Network.getByte();
				int height = Network.getByte();
				
				int[][] grid = new int[height][width];
				
				for(int row = 0; row &lt; grid.length; row++) {
					for(int column = 0; column &lt; grid[row].length; column++) {
						if(Network.getBoolean()) {
							grid[row][column] = WALL;
						} else {
							grid[row][column] = SPACE;
						}
					}
				}
				
				int tanks = Network.getByte();
				for(int i = 0; i &lt; tanks; i++) {
					int tankX = Network.getByte();
					int tankY = Network.getByte();
					grid[tankY][tankX] = TANK;
				}
				
				int shots = Network.getByte();
				for(int i = 0; i &lt; shots; i++) {
					int shotX = Network.getByte();
					int shotY = Network.getByte();
					grid[shotX][shotY] = Network.getByte();
				}
				
				//INSERT AI CODE HERE
				
			} else {
				Network.sendByte((byte) 0);
				System.out.println(&quot;We Are Dead&quot;);
			}
		}
	}
}
</pre>