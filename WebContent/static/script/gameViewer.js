/*
 * Handles the canvas updates for the main page and socket data
 */

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

//disconnects the websocket from the server
function disconnectWebSocket() {
	socket.close();
}

var pixelSize = document.getElementById("gameCanvas").width;

function displayMessage(message) {
	canvas.font = "30px Verdana";
	canvas.fillStyle = "Black";
	canvas.textBaseline = "middle";
	canvas.textAlign = "center";
	
	canvas.clearRect(0, 0, pixelSize, pixelSize);
	canvas.fillText(message, pixelSize / 2, pixelSize / 2, pixelSize / 2);
}

function canvasError() {
	displayMessage("Error connecting to server");
}

function canvasNoGame() {
	displayMessage("No game selected");
}

var timer = null;

function watchGame(gameSlot) {
	if(timer != null)
		clearInterval(timer);
	
	buffer = new ArrayBuffer(1);
	new DataView(buffer).setUint8(0, gameSlot);
	socket.send(buffer);
	timer = setInterval(updatePlayerList, 5000);
	setActiveStatus(gameSlot);
	updatePlayerList();
}

