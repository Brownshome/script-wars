package brownshome.scriptwars.game.snakes;

import java.io.IOException;

import com.liamtbrand.snake.controller.IStage;
import com.liamtbrand.snake.model.concrete.Stage;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.Direction;

/**
 * This is purely so that I have something to connect
 * to the server so I can see the map.
 * @author liamtbrand
 *
 */
public class EmptyTestAI {
	private static Network network;
	
	private static IStage stage;
	
	public static void main(String[] args) throws IOException {
		int id = 65536;

		network = new Network(id, "localhost", "Test AI");
		Direction direction = Direction.UP;
		System.out.println("Connecting");
		
		while(network.nextTick()) {
			stage = new Stage(new BasicMapModel(network));
			
			network.sendByte(direction.ordinal());
		}
		
		System.out.println(network.getConnectionStatus()); //This function may not be available on some languages.
	}
	
}
