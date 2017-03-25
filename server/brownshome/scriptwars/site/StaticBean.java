package brownshome.scriptwars.site;

import java.util.Collection;

import com.sun.javafx.scene.traversal.Direction;

import brownshome.scriptwars.server.game.GameType;
import brownshome.scriptwars.server.game.tanks.Shot;

public class StaticBean {
	public Collection<GameType> getTypeList() {
		return GameType.getGameTypes();
	}
	
	public String getTankGameShotSpeed() {
		return String.valueOf(Shot.SPEED);
	}
	
	public Direction[] getTankGameDirections() {
		return Direction.values();
	}
}
