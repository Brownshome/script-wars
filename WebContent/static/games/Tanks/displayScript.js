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
		shot(x, y);
		break;
	default:
		if(c - 3 < 10) {
			tank(x, y, c - 3);
		} else {
			canvasError();
		}
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

var colours = [
	[255, 99, 71], //red
	[0, 128, 0], //green
	[64, 224, 208], //cyan
	[0, 0, 255], //blue
	[148, 0, 211], //violet
	[255, 20, 147], //pink
	[128, 128, 0], //yellow
	[0, 0, 0], //black
	[75, 0, 130], //indigo
	[255, 165, 0] //orange
];

function tank(x, y, k) {
	canvas.drawImage(images.tank, x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
	var imageData = canvas.getImageData(x * pixelSize / width, y * pixelSize / height, pixelSize / width, pixelSize / height);
	for(var i = 0; i < imageData.data.length; i += 4) {
		if(imageData.data[i] < 100) {
			imageData.data[i] = colours[k][0];
			imageData.data[i + 1] = colours[k][1];
			imageData.data[i + 2] = colours[k][2];
		}
	}
	
	canvas.putImageData(imageData, x * pixelSize / width, y * pixelSize / height);
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

