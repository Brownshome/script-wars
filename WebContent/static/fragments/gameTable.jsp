<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
<c:if test="${empty gametype.games}" var="noGames">
	<b>No Active Games</b>
</c:if>
<c:if test="${!noGames}">
	<table>
		<tr>
			<th>Game Slot</th>
			<th>Players</th>
			<th>
				<!-- Extra column for watch button -->
			</th>
		</tr>
		<c:forEach varStatus="loopStatus" items="${gametype.games}" var="game">
			<tr>
				<!-- TODO highlight next game to join -->
				<td><c:out value="${loopStatus.index + 1}" /></td>
				<td><c:out value="Players ${game.playerCount}/${game.maximumPlayers}" /></td>
				<td><button onclick="watchGame(${game.slot})">Watch Game</button></td>
			</tr>
		</c:forEach>
	</table>
</c:if>
</body>