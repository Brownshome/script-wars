<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Script Wars - ${gametype.name}</title>
    <script src="../static/gameViewer.js" async></script>
    <script src="../static/games/${gametype.name}/displayScript.js" async></script>
</head>
<body onload="onLoad('${gametype.name}')">
    <h1>
        <img src="../static/games/${gametype.name}/icon.png" alt="${gametype.name} icon" style="width:64px;height:64px;">
        Game Type - ${gametype.name}
    </h1>
    <canvas id="gameCanvas" width="512" height="512"></canvas>
    
    <p><button id="UserID" onclick="requestID('${gametype.name}')">Request An ID</button></p>
	
	<h2>Active Games</h2>

    <div id="playerTable"></div>

	<div id="gameTable">
	   <jsp:include page="/static/gameTable.jsp" />
	</div>
	<h2>How To Compete</h2>
	<p> Download the client library <a href="https://raw.githubusercontent.com/Brownshome/script-wars/master/client/java/brownshome/scriptwars/client/Network.java">
	here</a>. It needs to be placed in a <b>brownshome/scriptwars/client</b> folder next to your AI code. Pick a game to play from the list above.
	Use the functions in the Network class documented <a href="/doc/brownshome/scriptwars/client/Network.html">here</a> to create an AI. Request 
	an ID from the game page and use it in the connect function as shown in the documentation. Compile your code with javac and run it.</p>
	<p>Use <code>Network.connect(ID, "13.55.154.170", 35565, "John Smith");</code> to connect to the server.</p>
	
	<jsp:include page="/static/games/${gametype.name}/rules.jsp" />
    
</body>
</html>