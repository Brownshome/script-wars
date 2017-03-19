<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Script-Wars</title>
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
        
        <table>
            <tr>
                <%-- Use EL instead, apparently this stuff is bad...--%>
                <%@ page import = "brownshome.scriptwars.server.Server, brownshome.scriptwars.server.game.GameType" %>
                <% for(GameType game : Server.getGames()) { %>
                <td>
                    <a href="games/<%= game.getName() %>">
                        <img src="<%= game.getName() %>.png" alt="<%= game.getName() %>" style="width:128px;height:128px;">
                        <h3>Tanks</h3>
                    </a>
                </td>
                <% } %>
            <tr>
        </table>
	</body>
</html>