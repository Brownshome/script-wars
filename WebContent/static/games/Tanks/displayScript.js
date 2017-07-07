/**
 * The display script for Tanks. This code displays sprites in a grid based canvas.
 */

var images = {};
var idList = null;

//Called on page load
function onLoad(gameID) {
	displayMessage("Loading Sprites");
	let sprites = {
		bullet: "../static/games/Tanks/bullet.png", 
	};
	
	loadSprites(sprites, gameID);
}

function loadSprites(sprites, gameID) {
	let numImages = 0;
	// get num of sources
	for(name in sprites) {
		numImages++;
	}

	for(name in sprites) {
		images[name] = new Image();
		images[name].onload = function() {
			if(--numImages <= 0) {
				connectWebSocket(gameID);
			}
		};
		
		images[name].src = sprites[name];
	}
}

/*
 * 4: Player ID list
 * 0: Bulk update
 * 1: Delta update
 * 
 * 2: Update game table
 * 3: Update player table
 */
function onMessage(message) {
	let dataView = new DataView(message.data);
	let header = dataView.getUint8(0);
	
	switch(header) {
	case 0: //bulk
		handleBulkData(dataView);
		break;
	case 1: //delta
		handleDeltaData(dataView);
		break;
	case 2:
		updateGameTable();
		break;
	case 3:
		updatePlayerList();
		break;
	case 4:
		updateIDMap(dataView);
		break;
	case 5:
		disconnectWebSocket();
		canvasNoGame();
		break;
	default:
		canvasError();
	}
}

function updateIDMap(dataView) {
	idList = [];
	let length = dataView.getUint8(1);
	
	for(index = 0; index < length; index++) {
		let id = dataView.getUint32(index * 4 + 2).toString();
		idList.push(id);
		
		if(id == "0") {
			images[id] = null;
		} else {
			images[id] = new Image();
			images[id].src = "/playericon/" + id;
		}
	}
}

function handleBulkData(dataView) {
	canvas.clearRect(0, 0, pixelSize, pixelSize);
	
	width = dataView.getUint8(1);
	
	height = dataView.getUint8(2);
	
	for(y = 0; y < height; y++) {
		for(x = 0; x < width; x++) {
			let c = dataView.getUint16(3 + (x + y * width) * 2);
			paintItem(x, y, c);
		}
	}
}

function paintItem(x, y, c) {
	switch(c) {
	case 0: //space
		clear(x, y);
		break;
	case 1: //wall
		wall(x, y);
		break;
	case 2: //bullet
		shot(x, y);
		break;
	default:
		tank(x, y, c - 3);
	}
}

function handleDeltaData(dataView) {
	let offset = 1;
	
	while(offset <= dataView.byteLength - 4) {
		let c = dataView.getUint16(offset);
		let x = dataView.getUint8(offset + 2);
		let y = dataView.getUint8(offset + 3);
		
		paintItem(x, y, c);
		
		offset = offset + 4;
	}
}

function tank(x, y, index) {
	clear(x, y);
	
	if(idList == null)
		return;
	
	if(idList.length <= index) {
		return; //we haven't been sent that ID yet
	}
	
	let image = images[idList[index]];
	
	if(image == null) {
		return;
	}
	
	if(image.complete) {
		canvas.drawImage(image, x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
	} else {
		image.onload = function() {
			tank(x, y, index);
		};
	}
}

function wall(x, y) {
	canvas.fillStyle = 'black';
	canvas.fillRect(x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
}
	
function clear(x, y) {
	canvas.clearRect(x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
}

function shot(x, y) {
	clear(x, y);
	
	canvas.drawImage(images.bullet, x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
}

