
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

SnakeGameDisplayHandler.prototype.getStaticSprite = function(data, x, y) {
	if(data == 0)
		return null;
	
	return new ColouredBlock(x, y, x, y, 0, this, 'black');
};

SnakeGameDisplayHandler.prototype.getDynamicSprite = function(data, sx, sy, ex, ey) {
	switch(data) {
		case 0: return new ColouredBlock(sx, sy, ex, ey, 0, this, 'green');
		case 1: return new ColouredBlock(sx, sy, ex, ey, 1, this, 'red');
		case 2: return new ColouredBlock(sx, sy, ex, ey, 0, this, 'blue');
		case 3: return new ColouredBlock(sx, sy, ex, ey, 0, this, 'lightblue');
	}
};

function onLoad() {
	displayHandler = new SnakeGameDisplayHandler();
	GridSprite.frameTime = 250; //TODO sync this value from the server, maybe using jsp?
	displayHandler.startRender();
}