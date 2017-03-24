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
                <jsp:useBean id="gameTypes" class="brownshome.scriptwars.site.GameTypeList"/>
                
                <c:forEach items="${gameTypes.typeList}" var="gameType">
                <td>
                    <a href="games/${gameType.name}"><img title="${gameType.description}" src="${gameType.name}.png" alt="${gameType.name}" style="width:128px;height:128px;"></a>
                    <br>${gameType.playerCount} players online.
                </td>
                </c:forEach>
            <tr>
        </table>
	</body>
</html>