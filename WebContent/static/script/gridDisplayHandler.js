/**
 * This object extends the displayHandler to handle grid based commands
 */
function GridDisplayHandler(gameID) {
	DisplayHandler.call(this, gameID);
	
	this.lastDeltaUpdate = null;
}

GridDisplayHandler.prototype = Object.create(DisplayHandler.prototype);
GridDisplayHandler.prototype.constructor = GridDisplayHandler;

GridDisplayHandler.prototype.bulkUpdate = function(dataView) {
	this.lastDeltaUpdate = null;
	
	this.width = dataView.getUint8();
	this.height = dataView.getUint8(1);
	
	//Ensure that each grid element is square
	this.size = Math.min(
			this.canvas.width / this.width,
			this.canvas.height / this.height
	);
	
	this.grid = [];
	
	for(let y = 0; y < this.height; y++) {
		this.grid[y] = [];
		for(let x = 0; x < this.width; x++) {
			const c = dataView.getUint8(2 + (x + y * this.width));
			this.grid[y][x] = this.getSprite(c, x, y, 0, 0);
		}
	}
};

GridDisplayHandler.prototype.convertX = function(x) {
	return (this.canvas.width - this.size * this.width) / 2 + x * this.size;
}

GridDisplayHandler.prototype.convertY = function(y) {
	return (this.canvas.height - this.size * this.height) / 2 + y * this.size;
}

GridDisplayHandler.prototype.startRender = function() {
	requestAnimationFrame(() => this.render());
}

GridDisplayHandler.prototype.render = function() {
	if(this.slot != null) {
		this.clearCanvas();
		for(let y = 0; y < this.height; y++) {
			for(let x = 0; x < this.width; x++) {
				if(this.grid[y][x]) this.grid[y][x].render();
			}
		}
	}
	
	requestAnimationFrame(() => this.render());
};

GridDisplayHandler.prototype.deltaUpdate = function(dataView) {
	let offset = 0;
	
	while(offset <= dataView.byteLength - 4) {
		const c = dataView.getUint8(offset);
		const x = dataView.getUint8(offset + 1);
		const y = dataView.getUint8(offset + 2);
		const dx = dataView.getInt8(offset + 3);
		const dy = dataView.getInt8(offset + 4);
		
		this.grid[y][x] = this.getSprite(c, x, y, dx, dy);
		
		offset = offset + 5;
	}

	this.lastDeltaUpdate = Date.now();
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

function ImageSprite(x, y, dx, dy, displayHandler, imageName) {
	GridSprite.call(this, x, y, displayHandler);
	
	this.dx = dx;
	this.dy = dy;
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
	/*
	 * This sprite may be animated using the dx, dy values. If so the sprite is rendered at (x - dx, y - dy) in the instant that the message
	 * is received and is smoothed to (x, y) over ImageSprite.frameTime time.
	 */
	
	//Maybe should cache Date.now this leads to every item being animated differently.
	
	let x;
	let y;

	if(ImageSprite.frameTime && this.displayHandler.lastDeltaUpdate != null) {
		const dt = (Date.now() - this.displayHandler.lastDeltaUpdate) / ImageSprite.frameTime;
		const lerp = Math.max(dt, 1);

		x = this.x - this.dx * (1 - dt);
		y = this.y - this.dy * (1 - dt);
	} else {
		x = this.x;
		y = this.y;
	}
	
	if(this.image.complete) {
		this.displayHandler.context.drawImage(
			this.image, 
			this.displayHandler.convertX(x), 
			this.displayHandler.convertY(y), 
			this.displayHandler.size,
			this.displayHandler.size
		);
	}
};