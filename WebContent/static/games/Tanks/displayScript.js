var displayHandler;

/** The tank game displayHandler */
function TankGameDisplayHandler() {
	GridDisplayHandler.call(this, "Tanks");
}

TankGameDisplayHandler.prototype = Object.create(GridDisplayHandler.prototype);
TankGameDisplayHandler.prototype.constructor = TankGameDisplayHandler;

/** Handles an incoming list of player IDs, an ID of zero means that the image is not used and will not be sent */
TankGameDisplayHandler.prototype.updatePlayerIDMap = function(dataView) {
	const length = dataView.getUint8(1); 

	/** Holds a mapping from player number to player ID */
	this.idList = [];
	for(let index = 0; index < length; index++) { 
		const id = dataView.getUint32(index * 4 + 2); 
		this.idList.push(id);

		if(id != 0 && !ImageSprite.prototype.sprites[id])
			ImageSprite.regesterSprite(id, "/playericon/" + id);
	} 
};

TankGameDisplayHandler.prototype.functionLookup = GridDisplayHandler.prototype.functionLookup.concat([
	TankGameDisplayHandler.prototype.updatePlayerIDMap
]);

function WallSprite(x, y, handler) {
	GridSprite.call(this, x, y, handler);
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

TankGameDisplayHandler.prototype.getSprite = function(data, x, y) {
	switch(data) {
		case 0: return null;
		case 1: return new WallSprite(x, y, this);
		case 2: return new ImageSprite(x, y, this, "bullet");
	}
	
	if(!this.idList)
		return undefined;
		
	let id = this.idList[data - 3]; /** No 3 is player 1 */
	
	if(id == 0)
		return undefined;
	
	//Player sprite
	return new ImageSprite(x, y, this, id);
};

function onLoad() {
	displayHandler = new TankGameDisplayHandler();

	ImageSprite.regesterSprite("bullet", "../static/games/Tanks/bullet.png");
	
	setInterval(() => displayHandler.render(), 20);
}