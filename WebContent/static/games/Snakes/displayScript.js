
var displayHandler;

/** The snake game displayHandler */
function SnakeGameDisplayHandler() {
	GridDisplayHandler.call(this, "Snakes");
}

SnakeGameDisplayHandler.prototype = Object.create(GridDisplayHandler.prototype);
SnakeGameDisplayHandler.prototype.constructor = SnakeGameDisplayHandler;

/** Handles an incoming list of player IDs, an ID of zero means that the image is not used and will not be sent */
SnakeGameDisplayHandler.prototype.updatePlayerIDMap = function(dataView) {
	/** Holds a mapping from player number to player ID */
	this.idList = [];
	
	let offset = 0;
	while(offset + 4 <= dataView.byteLength) {
		const id = dataView.getUint32(offset);
		offset += 4;
		this.idList.push(id);

		if(id != 0 && !ImageSprite.prototype.sprites[id])
			ImageSprite.regesterSprite(id, "../playericon/" + id);
	} 
};

SnakeGameDisplayHandler.prototype.functionLookup = GridDisplayHandler.prototype.functionLookup.concat([
	SnakeGameDisplayHandler.prototype.updatePlayerIDMap
]);

function WallSprite(x, y, handler) {
	GridSprite.call(this, x, y, 1, handler);
}

WallSprite.prototype = Object.create(GridSprite.prototype);
WallSprite.prototype.constructor = WallSprite;

WallSprite.prototype.render = function() {
	const displayHandler = this.displayHandler;

	displayHandler.context.fillStyle = 'black'; 
	displayHandler.context.fillRect(
			this.displayHandler.convertX(this.x), 
			this.displayHandler.convertY(this.y), 
			this.displayHandler.size,
			this.displayHandler.size
	);
};

SnakeGameDisplayHandler.prototype.getStaticSprite = function(data, x, y) {
	if(data == 0)
		return null;
	
	return new WallSprite(x, y, this);
};

SnakeGameDisplayHandler.prototype.getDynamicSprite = function(data, sx, sy, ex, ey) {
	switch(data) {
		case 0: return new ImageSprite(sx, sy, ex, ey, 0, this, "bullet");
		case 1: return new ImageSprite(sx, sy, ex, ey, 1, this, "ammoPickup");
	}
	
	if(!this.idList)
		return undefined;
		
	let id = this.idList[data - 2]; /** No 2 is player 0 */
	
	if(id == 0)
		return undefined;
	
	//Player sprite
	return new ImageSprite(sx, sy, ex, ey, 2, this, id);
};

function onLoad() {
	displayHandler = new SnakeGameDisplayHandler();
	ImageSprite.frameTime = 250; //TODO sync this value from the server, maybe using jsp?
	//ImageSprite.regesterSprite("bullet", "../static/games/Snakes/bullet.png");
	//ImageSprite.regesterSprite("ammoPickup", "../static/games/Snakes/ammoPickup.png");
	displayHandler.startRender();
}