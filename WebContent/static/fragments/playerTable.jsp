<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	</head>
	<body>
		<h2>Player List</h2>
		<c:if test="${fn:length(game.activePlayers) == 0}">
			<div class=text-center><p>There are no players active in this game.</p></div>
		</c:if>
		<c:if test="${fn:length(game.activePlayers) != 0}">
			<table class="table">
				<tr>
					<th></th>
					<th>Name</th>
					<th>Score</th>
					<th>Time Joined</th>
				</tr>

				<c:forEach items="${game.activePlayers}" var="player">
					<tr>
						<td><img src="/playericon/${player.ID}" style="width:35px;height:35px;"></td>
						<td><c:out value="${player.name}" /></td>
						<td><c:out value="${player.score}" /></td>
						<td><c:out value="${player.timeJoined}" /></td>
					</tr>
				</c:forEach>
			</table>
		</c:if>
	</body>
</html>