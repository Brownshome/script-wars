package brownshome.scriptwars.game;

import brownshome.scriptwars.game.tanks.Coordinates;

public interface GridItem {
	/**
	 * The code is the number that will be passed to the javascript engine
	 * @return
	 */
	byte code();
	/**
	 * The starting position that the item will be at the beginning of interpolation
	 * @return
	 */
	Coordinates start();
	/**
	 * The finishing position that the item will be at the end of interpolation
	 * @return
	 */
	Coordinates end();
}
