/**
 * This object extends the displayHandler to handle grid based commands
 */
function GridDisplayHandler(gameID) {
	DisplayHandler.call(this, gameID);
}

GridDisplayHandler.prototype = Object.create(DisplayHandler.prototype);
GridDisplayHandler.prototype.constructor = GridDisplayHandler;

GridDisplayHandler.prototype.staticUpdate = function(dataView) {
	this.dynamicObjects = null;
	
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
			this.grid[y][x] = this.getStaticSprite(c, x, y);
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
		
		for(let layer = 0; layer <= GridSprite.maxLayer; layer++) {
			for(let y = 0; y < this.height; y++) {
				for(let x = 0; x < this.width; x++) {
					if(this.grid[y][x] && this.grid[y][x].layer == layer) this.grid[y][x].render();
				}
			}
			
			if(this.dynamicObjects)
				for(let i = 0; i < this.dynamicObjects.length; i++) {
					if(this.dynamicObjects[i].layer == layer) this.dynamicObjects[i].render();
				}
		}
	}
	
	requestAnimationFrame(() => this.render());
};

GridDisplayHandler.prototype.dynamicUpdate = function(dataView) {
	this.dynamicObjects = [];
	
	let offset = 0;
	
	while(offset <= dataView.byteLength - 4) {
		const c = dataView.getUint8(offset);
		const sx = dataView.getUint8(offset + 1);
		const sy = dataView.getUint8(offset + 2);
		const ex = dataView.getInt8(offset + 3);
		const ey = dataView.getInt8(offset + 4);
		
		this.dynamicObjects.push(this.getDynamicSprite(c, sx, sy, ex, ey));
		
		offset = offset + 5;
	}

	this.dynamicObjects.lastUpdate = Date.now();
};

GridDisplayHandler.prototype.functionLookup = DisplayHandler.prototype.functionLookup.concat([
	GridDisplayHandler.prototype.staticUpdate,
	GridDisplayHandler.prototype.dynamicUpdate
]);

/**
 * Represents a single object in the grid 
 **/
function GridSprite(x, y, layer, displayHandler) {
	this.x = x;
	this.y = y;
	this.layer = layer;
	GridSprite.maxLayer = Math.max(GridSprite.maxLayer, layer);
	
	this.displayHandler = displayHandler;
}

GridSprite.maxLayer = 0;

function ImageSprite(sx, sy, ex, ey, layer, displayHandler, imageName) {
	GridSprite.call(this, ex, ey, layer, displayHandler);
	
	this.sx = sx;
	this.sy = sy;
	this.ex = ex;
	this.ey = ey;
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
	
	//Maybe should cache Date.now this leads to every item being animated differently
	const dt = (Date.now() - this.displayHandler.dynamicObjects.lastUpdate) / ImageSprite.frameTime;
	const lerp = Math.max(dt, 1);

	const x = this.ex * dt + this.sx * (1 - dt);
	const y = this.ey * dt + this.sy * (1 - dt);
	
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