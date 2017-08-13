package brownshome.scriptwars.game.snakes;

import java.io.IOException;

import com.liamtbrand.snake.controller.IStage;
import com.liamtbrand.snake.model.IMapModel;
import com.liamtbrand.snake.model.ISnakeModel.Direction;
import com.liamtbrand.snake.model.concrete.Stage;
import com.liamtbrand.snake.model.concrete.test.TestMap;

import brownshome.scriptwars.connection.Network;

public class TestAI {
	private static Network network;
	
	private static IStage stage;
	
	public static void main(String[] args) throws IOException {
		
		IMapModel model = new BasicMapModel(20,20);
		stage = new Stage(model);
		
		String idString = "65536";

		int id;
		id = Integer.parseInt(idString);

		network = new Network(id, "localhost", "Test AI");
		Direction direction = Direction.NORTH;
		System.out.println("Connecting");

		while(network.nextTick()) {
			
			// Get Map
			int mapWidth = network.getByte() & (0xff);
			int mapHeight = network.getByte() & (0xff);
			System.out.println("Map Width: "+mapWidth+", Map Height: "+mapHeight);
			int ptr = 8;
			int BYTE_LENGTH = 8;
			byte b = 0;
			BasicMapModel map = (BasicMapModel) stage.getMap();
			for(int y = 0; y < mapHeight; y++) {
				for(int x = 0; x < mapWidth; x++) {
					if (ptr == BYTE_LENGTH) {
						b = (byte) network.getByte();
						ptr = 0;
					}
					map.setWall(x, y, (b & (1 << ptr)) >> ptr == 1 );
					ptr++;
				}
			}
			
			while(network.hasData()) { // flush the data.
				System.out.println(network.getByte());
			}
			
			network.sendByte(direction.ordinal());
		}
		
		System.out.println(network.getConnectionStatus()); //This function may not be available on some languages.
	}
	
}
