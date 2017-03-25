<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

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
    <p id="UserID" onclick="requestID('${gametype.name}')">Request an ID</p>
    <h2>Rules</h2>
    <jsp:include page="/Tanks-rules.jsp"/>
</body>
</html>