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
	this.context = this.canvas.getContext("2d");
	this.playerTable = document.getElementById("playerTable");
	this.socket.onmessage = (message) => this.onMessage(message);
	this.socket.onclose = () => this.displayNoGame();
	this.socket.onerror = () => this.displayError();
	this.socket.binaryType = "arraybuffer";
	
	this.displayNoGame();
}

DisplayHandler.prototype = Object.create(Object.prototype);

/** Sets the current game to display on the canvas */
DisplayHandler.prototype.watchGame = function(slot) {
	if(this.slot == slot)
		return;
	
	this.slot = slot;
	const buffer = new ArrayBuffer(1);
	new DataView(buffer).setUint8(0, this.slot == null ? -1 : this.slot);
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
	
	this.clearCanvas();
	this.context.fillText(message, this.canvas.width / 2, this.canvas.height / 2, this.canvas.width / 2);
};

DisplayHandler.prototype.clearCanvas = function() {
	this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
};

DisplayHandler.prototype.displayError = function () {
	this.displayMessage("Error connecting to server. Please refresh.");
};

DisplayHandler.prototype.displayNoGame = function() {
	this.clearPlayerTable();
	this.slot = null;
	this.displayMessage("No game selected.");
};

DisplayHandler.prototype.updatePlayerList = function() {
	if(this.slot == null)
		return;
	
	const request = new XMLHttpRequest();
	
	request.onreadystatechange = () => {
		if(this.slot == null)
			return;
		
		if(request.readyState == 4 && request.status == 200) {
			this.playerTable.innerHTML = request.responseText;
		}
	};
	
	request.open("GET", "../playertable/" + this.slot, true);
	request.send();
};

DisplayHandler.prototype.updatePlayerScores = function(dataView) {
	if(this.slot == null)
		return;

	for(let index = 0; index * 8 <= dataView.byteLength - 8; index++) {
		const id = dataView.getInt32(index * 8)
		const score = dataView.getInt32(index * 8 + 4);
		
		let player = document.getElementById("Score-" + id);
		if(!player) player.innerHTML = score;
	} 
};

/**
 * The first uint8 is a purpose code used to lookup the correct function to call
 */
DisplayHandler.prototype.onMessage = function(message) {
	const dataView = new DataView(message.data);
	const header = dataView.getUint8(0);
	
	if(header in this.functionLookup) {
		this.functionLookup[header].call(this, new DataView(message.data, 1));
	} else {
		this.canvasError();
	}
};

/**
 * 0: updateGameTable
 * 1: updatePlayerTable
 * 2: disconnect
 * 3: updateScores
 */
DisplayHandler.prototype.functionLookup = [
	AJAX.updateGameTable,
	DisplayHandler.prototype.updatePlayerList,
	DisplayHandler.prototype.displayNoGame,
	DisplayHandler.prototype.updatePlayerScores
];
