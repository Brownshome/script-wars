/**
 * The display script for Tanks. This code displays sprites in a grid based canvas.
 */

var images = {};

//Called on page load
function onLoad(gameID) {
	displayMessage("Loading Sprites");
	var sprites = {
			bullet: "../static/games/Tanks/bullet.png", 
			tank: "../static/games/Tanks/icon.png"
	};
	loadSprites(sprites, gameID);
}

function loadSprites(sprites, gameID) {
	var numImages = 0;
	// get num of sources
	for(var src in sprites) {
		numImages++;
	}

	for(var src in sprites) {
		images[src] = new Image();
		images[src].onload = function() {
			if(--numImages <= 0) {
				connectWebSocket(gameID);
			}
		};
		
		images[src].src = sprites[src];
	}
}

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
	case 2:
		updateTable();
		break;
	default:
		canvasError();
	}
}

function handleBulkData(dataView) {
	canvas.clearRect(0, 0, pixelSize, pixelSize);
	
	width = dataView.getUint8(1);
	height = dataView.getUint8(2);
	
	for(y = 0; y < height; y++) {
		for(x = 0; x < width; x++) {
			var c = dataView.getUint16(3 + (x + y * width) * 2);
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
		fill(x, y, images.bullet);
		break;
	case 3: //tank
		fill(x, y, images.tank);
		break;
	default:
		canvasError();
	}
}

function handleDeltaData(dataView) {
	var offset = 1;
	
	while(offset <= dataView.byteLength - 4) {
		var c = dataView.getUint16(offset);
		var x = dataView.getUint8(offset + 2);
		var y = dataView.getUint8(offset + 3);
		
		paintItem(x, y, c);
		
		offset = offset + 4;
	}
}

function wall(x, y) {
	canvas.fillStyle = 'black';
	canvas.fillRect(x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
}
	
function clear(x, y) {
	canvas.clearRect(x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
}

function fill(x, y, image) {
	clear(x, y);
	
	canvas.drawImage(image, x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
}

