<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body><%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h2>Player List</h2>
<table>
	<tr>
		<th>Name</th>
		<th>Colour</th>
		<th>Time Joined</th>
		<th>Score</th>
	</tr>
	<c:forEach items="${game.activePlayers}" var="player">
		<tr>
			<td><c:out value="${player.name}" /></td>
            <td><c:out value="${player.colour}" /></td>
            <td><c:out value="${player.timeJoined}" /></td>
            <td><c:out value="${player.score}" /></td>
		</tr>
	</c:forEach>
</table>
</body>