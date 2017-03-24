package brownshome.scriptwars.site;

import java.util.Collection;

import brownshome.scriptwars.server.game.GameType;

public class GameTypeList {
	public Collection<GameType> getTypeList() {
		return GameType.getGameTypes();
	}
}
