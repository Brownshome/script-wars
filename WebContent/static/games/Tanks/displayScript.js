var displayHandler;

/** The tank game displayHandler */
function TankGameDisplayHandler() {
	GridDisplayHandler.call(this);
}

TankGameDisplayHandler.prototype = Objects.create(GridDisplayHandler);
TankGameDisplayHandler.prototype.constructor = TankGameDisplayHandler;

TankGameDisplayHandler.prototype.functionLookup = GridDisplayHandler.prototype.functionLookup.concat([
	(data) => this.updatePlayerIDMap(data)
	]);

/** Handles an incoming list of player IDs, an ID of zero means that the image is not used and will not be sent */
TankGameDisplayHandler.prototype.updatePlayerIDMap = function(data) {
	const length = dataView.getUint8(1); 

	/** Holds a mapping from player number to player ID */
	this.idList = [];
	for(let index = 0; index < length; index++) { 
		const id = dataView.getUint32(index * 4 + 2); 
		idList.push(id);

		if(id != 0 && !ImageSprite.prototype.sprites[id])
			ImageSprite.regesterSprite(id, "/playericon/" + id);
	} 
};

function WallSprite(x, y, handler) {
	GridSprite.call(this, x, y, handler);
}

WallSprite.prototype = Objects.create(GridSprite.prototype);
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

TankGameDisplayHandler.prototype.getSprite = function(data, x, y) {
	if(data == 1)
		return new WallSprite(x, y, this);
	
	if(!this.idList)
		return undefined;
		
	let id = this.idList[data];
	
	if(id == 0)
		return undefined;
	
	//Player sprite
	return new ImageSprite(x, y, this, id);
};

function onLoad() {
	displayHandler = new TankGameDisplayHandler();

	ImageSprite.regesterSprite("bullet", "../static/games/Tanks/bullet.png");
}