<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean"/>

<h2>About</h2>
<p>
Snakes game.
</p>
<hr>

<h2>Rules</h2>
<p>
</p><p>
</p><p>
</p><p>
</p><p>
</p><p>
</p><p>
The coordinate system is defined so that (0, 0) is at the top left of the game board. So
to move UP subtract one from your y coordinate.
</p>
<hr>

<h2>On Each Game Tick</h2>
<div class="media">
	<div class="media-left"><h1>1.</h1></div>
	<div class="media-body media-middle"><p>
	</p></div>
</div>
<div class="media">
	<div class="media-left"><h1>2.</h1></div>
	<div class="media-body media-middle"><p>
	</p></div>
</div>
<div class="media">
	<div class="media-left"><h1>3.</h1></div>
	<div class="media-body media-middle"><p>
	</p></div>
</div>
<hr>

<h2>Data protocol</h2>
<p>Currently the only option for connecting is to use the Network class.
This is the lower level API that all games use and allows access to the raw data sent from the server.</p>

<div class="panel panel-default"><div class="panel-body">
<ul class="nav nav-tabs">
	<li class="active">
		<a href="#basic" data-toggle="tab">
			<strong>Low Level / Cross Code</strong>
		</a>
	</li>
</ul>

<div class="tab-content">
	<div class="tab-pane active" id="basic">
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
		
		<p>For the snake game the data received from the server is laid out in the table below.</p>
		<table class="table">
			<tr>
				<th>Name</th>
				<th>Amount</th>
				<th>Type</th>
				<th>Meaning</th>
			</tr>
			<tr>
				<td>mapWidth</td>
				<td>1</td>
				<td>Byte</td>
				<td>The width of the game grid.</td>
			</tr>
			<tr>
				<td>mapHeight</td>
				<td>1</td>
				<td>Byte</td>
				<td>The height of the game grid.</td>
			</tr>
			<tr>
				<td>isWall</td>
				<td>mapWidth * mapHeight</td>
				<td>Boolean</td>
				<td>True if there is a wall, False otherwise. The values are from top to bottom, left to right, row by row. e.g. (0, 0) to (mapWidth - 1, 0) then (1, 0)</td>
			</tr>
			<tr>
				<td>numberOfSnakes</td>
				<td>1</td>
				<td>Byte</td>
				<td>The number of snakes to be received. For each snake, read the ID, the length, and then read the segments for the length of the snake.</td>
			</tr>
			<tr>
				<td>snakeId</td>
				<td>1</td>
				<td>Byte (Integer?)</td>
				<td>The ID of the snake we are reading.</td>
			</tr>
			<tr>
				<td>snakeLength</td>
				<td>1</td>
				<td>Byte (Integer?)</td>
				<td>The length of the snake we are reading.</td>
			</tr>
			<tr>
				<td>segmentX</td>
				<td>1</td>
				<td>Byte</td>
				<td>The x position on the grid of the current snakes nth segment.</td>
			</tr>
			<tr>
				<td>segmentY</td>
				<td>1</td>
				<td>Byte</td>
				<td>The y position on the grid of the current snakes nth segment.</td>
			</tr>
			<tr>
				<td>numberOfGameObjects</td>
				<td>1</td>
				<td>Byte</td>
				<td>The number of game objects to be received. For each game object, read the x and y coordinates.</td>
			</tr>
			<tr>
				<td>gameObjectX</td>
				<td>1</td>
				<td>Byte</td>
				<td>The x position on the grid of the game object being received.</td>
			</tr>
			<tr>
				<td>gameObjectY</td>
				<td>1</td>
				<td>Byte</td>
				<td>The y position on the grid of the game object being received.</td>
			</tr>
			<tr>
				<td>gameObjectType</td>
				<td>1</td>
				<td>Byte</td>
				<td>The type of the game object being received.</td>
			</tr>
		</table>
		
		<p>Each turn one byte must be sent to the server.</p>
		<table class="table">
			<tr>
				<th>Name</th>
				<th>Amount</th>
				<th>Type</th>
				<th>Meaning</th>
			</tr>
			<tr>
				<td>direction</td>
				<td>1</td>
				<td>Byte</td>
				<td>The direction that the snake should move.</td>
			</tr>
		</table>
		
		<p>The values of the direction byte, and game object types are given below.</p>
		<div class="row"><div class="col-md-6">
			<h4>Direction Byte</h4>
			<table class="table">
				<tr>
					<th>Value</th>
					<th>Meaning</th>
				</tr><tr>
					<td>0</td><td>North</td>
				</tr><tr>
					<td>1</td><td>South</td>
				</tr><tr>	
					<td>2</td><td>East</td>
				</tr><tr>	
					<td>3</td><td>West</td>
				</tr>
			</table>
		</div><div class="col-md-6">
			<h4>Game Object Types</h4>
			<table class="table">
				<tr>
					<th>Value</th>
					<th>Meaning</th>
				</tr><tr>
					<td>0</td><td>Food</td>
				</tr><tr>
					<td>1</td><td>Wormhole</td>
				</tr>
			</table>
		</div></div>
		
	</div>
</div>
</div>
</div>