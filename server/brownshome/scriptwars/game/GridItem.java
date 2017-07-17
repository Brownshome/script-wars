package brownshome.scriptwars.game;

import brownshome.scriptwars.game.tanks.Coordinates;

public interface GridItem {
	byte getCode();
	Coordinates getMove();
}
