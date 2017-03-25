<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Script Wars - ${gametype.name}</title>
    <script src="../gameViewer.js" async></script>
</head>
<body onload="connectWebSocket('${gametype.name}')">
    <h1>
        <img src="../${gametype.name}.png" alt="${gametype.name} icon" style="width:64px;height:64px;">
        Game Type - ${gametype.name}
    </h1>
    <canvas id="gameCanvas" width="256" height="256"></canvas>
    
    <p><button id="UserID" onclick="requestID('${gametype.name}')">Request An ID</button></p>
	
	<h2>Active Games</h2>
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
			<c:forEach varStatus="loopStatus" items="${gametype.games}"
				var="game">
				<tr>
					<!-- TODO highlight next game to join -->
					<td><c:out value="${loopStatus.index + 1}" /></td>
					<td><c:out value="Players ${game.connectionHandler.playerCount}/${game.maximumPlayers}" /></td>
					<td><button onclick="watchGame(${game.slot})">Watch Game</button></td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
	<jsp:include page="/Tanks-rules.jsp" />
</body>
</html>