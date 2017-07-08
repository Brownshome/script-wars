/**
 * This object handles the display of the canvas and any game specific displays
 * Object DisplayHandler(gameID)
 * @constructor
 * @extends Object
 **/
function DisplayHandler(gameID) {
	const WebSocketURL = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + window.location.pathname + "/../../gameviewer/" + gameID;
	
	this.socket = new WebSocket(WebSocketURL);
	this.gameID = gameID;
	this.slot = null;
	this.canvas = document.getElementById("gameCanvas");
	this.context = DisplayHandler.canvas.getContext("2d");
	this.playerTable = document.getElementById("playerTable");
	
	setInterval(this.updatePlayerList.bind(this), 2000);
	
	this.socket.onmessage = this.onMessage.bind(this);
	this.socket.onclose = this.displayNoGame.bind(this);
	this.socket.onerror = this.displayError.bind(this);
	this.socket.binaryType = "arraybuffer";
	
	this.playerTableRefreshCounter = setInterval(updatePlayerList, 5000);
}

/** Sets the current game to display on the canvas */
DisplayHandler.prototype.watchGame = function(slot) {
	if(this.slot == slot)
		return;
	
	this.slot = slot;
	var buffer = new ArrayBuffer(1);
	new DataView(buffer).setUint8(0, this.slot || -1);
	this.socket.send(buffer);
	GameTable.clickOnSlot(slot);
	
	this.updatePlayerList();
};

DisplayHandler.prototype.clearPlayerTable = function() {
	this.playerTable.innerHTML = "";
};

DisplayHandler.prototype.displayMessage = function(message) {
	this.context.font = "30px Verdana";
	this.context.fillStyle = "Black";
	this.context.textBaseline = "middle";
	this.context.textAlign = "center";
	
	this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
	this.context.fillText(message, this.canvas.width / 2, this.canvas.height / 2, this.canvas.width / 2);
};

DisplayHandler.prototype.displayError = function () {
	this.displayMessage("Error connecting to server. Please refresh.");
};

DisplayHandler.prototype.displayNoGame = function() {
	this.clearPlayerTable();
	clearInterval(playerTableRefreshCounter);
	this.canvasNoGame();
};

DisplayHandler.prototype.updatePlayerList = function() {
	if(this.slot == null)
		return;
	
	var request = new XMLHttpRequest();
	var objRef = this;
	
	request.onreadystatechange = function() {
		if(objRef.slot == null)
			return;
		
		if(this.readyState == 4 && this.status == 200) {
			DisplayHandler.playerTable.innerHTML = this.responseText;
		}
	};
	
	request.open("GET", "../playertable/" + this.gameID, true);
	request.send();
};