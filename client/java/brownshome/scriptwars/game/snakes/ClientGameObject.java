package brownshome.scriptwars.game.snakes;

import com.liamtbrand.snake.model.IGameObjectModel.Type;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.Coordinates;

public class ClientGameObject {
	public final Type type;
	public final Coordinates coord;
	
	public ClientGameObject(Network network) {
		coord = new Coordinates(network);
		type = Type.values()[network.getByte()];
	}
}
