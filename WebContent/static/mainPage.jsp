<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                <jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean"/>
                
                <c:forEach items="${staticBean.typeList}" var="gameType">
                <td>
                    <a href="games/${gameType.name}"><img title="${gameType.description}" src="static/games/${gameType.name}/icon.png" alt="${gameType.name}" style="width:128px;height:128px;"></a>
                    <br>${gameType.playerCount} player${gameType.playerCount == 1 ? "" : "s"} online.
                </td>
                </c:forEach>
            <tr>
        </table>
        
        <h2>How To Compete</h2>
        <p>Download the client library <a href="https://raw.githubusercontent.com/Brownshome/script-wars/master/client/java/brownshome/scriptwars/client/Network.java" download="Network.java">
        here</a>. It needs to be placed in a <b>brownshome/scriptwars/client</b> folder next to your AI code.
        Pick a game to play from the list above. Use the functions in the Network class documented 
        <a href="/doc/brownshome/scriptwars/client/Network.html">here</a> to create an AI. Request an ID from the game page and use it in
        the connect function as shown in the documentation. Compile your code with javac and run it.</p>
        
        <p>Use <code>Network.connect(ID, "13.55.154.170", 35565, "John Smith");</code> to connect to the server.</p>
        
        <h2>How to Contribute</h2>
        <p>This project is in the early stages of it's development and you are encouraged to take an active
        role in it's development. The Github page for the site can be found 
        <a href="https://github.com/Brownshome/script-wars">here</a> along with more information about how to
        contribute to the project. But to sum up the information, the project needs help writing the css pages
        for the site and creating games other than "Tank Game". Any other changes to the project to benefit
        ease of use and / or the user experience are also welcome.</p>
        <p>Also feel free to document any improvements or feedback <a href="https://github.com/Brownshome/script-wars/issues">here</a>.</p>
	</body>
</html>