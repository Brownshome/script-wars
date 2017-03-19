<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Insert title here</title>
	</head>
	<body>
		<h1>Script Wars</h1>
        
        <h2>Info</h2>
        <p>Script Wars is a AI design competition designed 
        for university software students. Participants 
        write small AI programs using the supplied library 
        and connect to a competition server. The AIs then 
        compete against each other in simple games for glory.</p>
    
        <h2>Available Games</h2>
        
        <%@ page import = "brownshome.scriptwars.server.Server" %>
        <%@ page import = "brownshome.scriptwars.server.game.GameType" %>
        
        <% int i = 0;
        for(GameType game : Server.getGames()) {
        	System.out.println(game.getName() + "<br>");
        } %>
        
        <p><%= System.currentTimeMillis() %></p>
	</body>
</html>