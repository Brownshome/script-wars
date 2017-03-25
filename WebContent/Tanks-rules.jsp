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