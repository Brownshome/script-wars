//This javascript file handles the scripting on the game page.

var socket;
var canvas = document.getElementById("gameCanvas").getContext("2d");

//Starts a websocket connection with the server
function connectWebSocket(gameID) {
	socket = new WebSocket(createWSURL(gameID));
	socket.onmessage = onMessage;
	socket.binaryType = "arraybuffer";
}

function createWSURL(gameID) {
	return ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + window.location.pathname + "/../../gameviewer/" + gameID;
}

//disconnections the websocket from the server
function disconnectWebSocket() {
	socket.close();
}

var i = 0;
var pixelSize = 256;
var width;
var height;

function onMessage(message) {
	var dataView = new DataView(message.data);
	var header = dataView.getUint8(0);
	
	switch(header) {
	case 1: //delta
		handleDeltaData(dataView);
		break;
	case 0: //bulk
		handleBulkData(dataView);
		break;
	default:
		canvasError();	
	}
	
	i = i + 1;
	document.getElementById("Counter").innerHTML = "Update Count: " + i;
}

function handleBulkData(dataView) {
	canvas.clearRect(0, 0, pixelSize, pixelSize);
	
	width = dataView.getUint8(1);
	height = dataView.getUint8(2);
	
	for(y = 0; y < height; y++) {
		for(x = 0; x < width; x++) {
			var c = dataView.getUint16(3 + (x + y * width) * 2);
			if(c != 32) {
				fill(x, y, c);
			}
		}
	}
}

function handleDeltaData(dataView) {
	var offset = 1;
	
	while(offset <= dataView.byteLength - 4) {
		var c = dataView.getUint16(offset);
		var x = dataView.getUint8(offset + 2);
		var y = dataView.getUint8(offset + 3);
		
		if(c == 32) {
			clear(x, y);
		} else {
			fill(x, y, c);
		}
		
		offset = offset + 4;
	}
}

function clear(x, y) {
	canvas.clearRect((x - 0.01) * pixelSize / width, (y - 0.01) * pixelSize / height, 1.02 * pixelSize / width, 1.02 * pixelSize / height);
}

function fill(x, y, c) {
	clear(x, y);
	
	var s = String.fromCharCode(c);
	canvas.font = pixelSize / Math.max(width, height) + "px 'CourierNew'";
	canvas.fillText(s, x * pixelSize / width, (y + 1) * pixelSize / height);
	
	//canvas.fillRect(x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
}

function canvasError() {
	canvas.fillText("Error connection to server", pixelSize / 4, pixelSize / 2);
}

//Requests an connectionID value from the server using AJAX
function requestID(gameID) {
	var request = new XMLHttpRequest();
	request.onreadystatechange = AJAXReady;
	request.open("POST", "../requestID", true);
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	request.send("id=" + gameID);
}

function AJAXReady() {
	if(this.readyState == 4 && this.status == 200) {
		document.getElementById("UserID").innerHTML = "ID: " + this.responseText + " (Click for another)";
	}
}