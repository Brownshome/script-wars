/**
 * This object extends the displayHandler to handle grid based commands
 */
function GridDisplayHandler(gameID) {
	DisplayHandler.call(this, gameID);
}

GridDisplayHandler.prototype = Object.create(DisplayHandler.prototype);
GridDisplayHandler.prototype.constructor = GridDisplayHandler;

GridDisplayHandler.prototype.bulkUpdate = function(dataView) {
	this.clearCanvas();
	
	this.width = dataView.getUint8(1);
	this.height = dataView.getUint8(2);
	
	//Ensure that each grid element is square
	this.size = Math.min(
			this.canvas.width / this.width,
			this.canvas.height / this.height
	);
	
	this.grid = [];
	
	for(let y = 0; y < this.height; y++) {
		this.grid[y] = [];
		for(let x = 0; x < this.width; x++) {
			const c = dataView.getUint16(3 + (x + y * this.width) * 2);
			this.grid[y][x] = this.getSprite(c, x, y);
		}
	}
};

GridDisplayHandler.prototype.convertX = function(x) {
	return (this.canvas.width - this.size * this.width) / 2 + x * this.size;
}

GridDisplayHandler.prototype.convertY = function(y) {
	return (this.canvas.height - this.size * this.height) / 2 + y * this.size;
}

GridDisplayHandler.prototype.render = function() {
	if(this.slot == null)
		return;
		
	this.clearCanvas();
	
	for(let y = 0; y < this.height; y++) {
		for(let x = 0; x < this.width; x++) {
			if(this.grid[y][x]) this.grid[y][x].render();
		}
	}
};

GridDisplayHandler.prototype.deltaUpdate = function(dataView) {
	let offset = 1;
	
	while(offset <= dataView.byteLength - 4) {
		const c = dataView.getUint16(offset);
		const x = dataView.getUint8(offset + 2);
		const y = dataView.getUint8(offset + 3);
		
		this.grid[y][x] = this.getSprite(c, x, y);
		
		offset = offset + 4;
	}
};

/**
 * 4: bulk Update
 * 5: delta Update
 */
GridDisplayHandler.prototype.functionLookup = DisplayHandler.prototype.functionLookup.concat([
	GridDisplayHandler.prototype.bulkUpdate,
	GridDisplayHandler.prototype.deltaUpdate
]);

/**
 * Represents a single object in the grid 
 **/
function GridSprite(x, y, displayHandler) {
	this.x = x;
	this.y = y;
	this.displayHandler = displayHandler;
}

function ImageSprite(x, y, displayHandler, imageName) {
	GridSprite.call(this, x, y, displayHandler);
	
	this.image = this.sprites[imageName]; 
}

ImageSprite.prototype = Object.create(GridSprite.prototype);
ImageSprite.prototype.constructor = GridSprite;

ImageSprite.prototype.sprites = {};

/** Starts the image loading process */
ImageSprite.regesterSprite = function(name, url) {
	const image = new Image();
	ImageSprite.prototype.sprites[name] = image;
	image.src = url;
};

ImageSprite.prototype.render = function() {
	const displayHandler = this.displayHandler;
	
	if(this.image.complete) {
		displayHandler.context.drawImage(
			this.image, 
			this.displayHandler.convertX(this.x), 
			this.displayHandler.convertY(this.y), 
			this.displayHandler.size,
			this.displayHandler.size
		);
	}
};