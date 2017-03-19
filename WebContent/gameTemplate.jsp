<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Script Wars - ${gametype.name}</title>
    <script src="../gameViewer.js" async></script>
</head>
<body onload="connectWebSocket(${gametype.id})">
    <h1>
        <img src="../${gametype.name}.png" alt="${gametype.name} icon" style="width:64px;height:64px;">
        Game Type - ${gametype.name}
    </h1>
    <canvas id="gameCanvas" width="256" height="256"></canvas>
    <p id="UserID" onclick="requestID(${gametype.id})">Request an ID</p>
    <p id="Counter">Update Count: 0</p>
</body>
</html>