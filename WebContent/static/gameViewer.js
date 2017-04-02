//This javascript file handles the scripting on the game page.

var socket;
var canvas = document.getElementById("gameCanvas").getContext("2d");

//Starts a websocket connection with the server
function connectWebSocket(gameID) {
	socket = new WebSocket(createWSURL(gameID));
	socket.onmessage = onMessage;
	socket.onopen = canvasNoGame;
	socket.onclose = canvasError;
	socket.onerror = canvasError;
	socket.binaryType = "arraybuffer";
}

function createWSURL(gameID) {
	return ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + window.location.pathname + "/../../gameviewer/" + gameID;
}

//disconnections the websocket from the server
function disconnectWebSocket() {
	socket.close();
}

var pixelSize = 1024;
var width;
var height;

function updateTable() {
	var request = new XMLHttpRequest();
	request.onreadystatechange = tableUpdateReady;
	request.open("GET", "../static/gameTable.jsp", true);
	request.send();
}

function tableUpdateReady() {
	if(this.readyState == 4 && this.status == 200) {
		document.getElementById("gameTable").innerHTML = this.responseText;
	}
}

function displayMessage(message) {
	canvas.clearRect(0, 0, pixelSize, pixelSize);
	canvas.fillText(message, pixelSize / 4, pixelSize / 2);
}

function canvasError() {
	displayMessage("Error connecting to server");
}

function canvasNoGame() {
	displayMessage("No game selected");
}

//Requests an connectionID value from the server using AJAX
function requestID(gameID) {
	var request = new XMLHttpRequest();
	request.onreadystatechange = AJAXReady;
	request.open("POST", "../requestID", true);
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	request.send("type=" + gameID);
}

function AJAXReady() {
	if(this.readyState == 4 && this.status == 200) {
		document.getElementById("UserID").innerHTML = "ID: " + this.responseText + " (Click for another)";
	}
}

function watchGame(gameSlot) {
	buffer = new ArrayBuffer(1);
	new DataView(buffer).setUint8(0, gameSlot);
	socket.send(buffer);
}