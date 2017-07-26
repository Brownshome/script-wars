<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="root" value=".." scope="request"/>

<!DOCTYPE html>
<html>
<body>
<table class="table table-hover">
	<tr>
		<th class="text-center"><span class="glyphicon glyphicon-lock"></span></th>
		<th>Slot</th>
		<th>Players</th>
		<th><!-- Buttons --></th>
	</tr>
	<c:forEach varStatus="loopStatus" items="${gametype.games}" var="game">
		<tr id="entry-${game.slot}">
			<td><!-- Add lock icon for private games --></td>
			<td><c:out value="${loopStatus.index + 1}" /></td>
			<td><c:out value="Players ${game.playerCount} / ${game.maximumPlayers}" /></td>
			<td>
				<button onclick="displayHandler.watchGame(${game.slot})" class="btn btn-primary">Watch</button>
				<c:if test="${game.spaceForPlayer}">
					<button onclick="AJAX.requestSpecificID(${game.slot})" class="btn btn-primary">Join</button>
					
					<div class="btn-group">
						<button type="button" class="btn btn-danger dropdown-toggle" data-toggle="dropdown"
						aria-haspopup="true" aria-expanded="false">Add an enemy<span class="caret"></span>
						</button>
						
						<ul class="dropdown-menu">
							<c:forEach items="${gametype.difficulties}" var="difficulty">
								<li><a href="#" onclick="AJAX.addServerBot('${difficulty}', '${game.slot}')">${difficulty}</a></li>
							</c:forEach>
						</ul>
					</div>
				</c:if>
			</td>
		</tr>
	</c:forEach>
</table>
<div class="row"><div class="col-md-12 text-center">
	<button onclick="AJAX.requestID('${gametype.name}')" class="btn btn-lg btn-primary">Join a game</button>
</div></div>
</body>
</html>