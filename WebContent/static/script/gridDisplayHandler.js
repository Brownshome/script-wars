/**
 * This object extends the displayHandler to handle grid based commands
 */
function GridDisplayHandler() {
	DisplayHandler.call(this);
}

GridDisplayHandler.prototype = Objects.create(DisplayHandler.prototype);
GridDisplayHandler.prototype.constructor = GridDisplayHandler;

/**
 * 3: bulk Update
 * 4: delta Update
 */
GridDisplayHandler.prototype.functionLookup = DisplayHandler.prototype.functionLookup.concat([
	(data) => this.bulkUpdate(data),
	(data) => this.deltaUpdate(data)
]);

GridDisplayHandler.prototype.bulkUpdate = function(data) {
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
		grid[y] = [];
		for(let x = 0; x < this.width; x++) {
			const c = dataView.getUint16(3 + (x + y * width) * 2);
			if(c != 0) grid[y][x] = this.getSprite(c, x, y);
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
	this.clearCanvas();
	
	for(let y = 0; y < this.height; y++) {
		for(let x = 0; x < this.width; x++) {
			if(grid[y][x]) grid[y][x].render();
		}
	}
};

GridDisplayHandler.prototype.deltaUpdate = function(data) {
	let offset = 1;
	
	while(offset <= dataView.byteLength - 4) {
		const c = dataView.getUint16(offset);
		const x = dataView.getUint8(offset + 2);
		const y = dataView.getUint8(offset + 3);
		
		if(c != 0) grid[y][x] = this.getSprite(c, x, y);
		
		offset = offset + 4;
	}
};

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

ImageSprite.prototype = Objects.create(GridSprite.prototype);
ImageSprite.prototype.constructor = GridSprite;

ImageSprite.prototype.sprites = {};

/** Starts the image loading process */
ImageSprite.regesterSprite = function(name, url) {
	const image = new Image();
	ImageSprites.prototype.sprites[name] = image;
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