package brownshome.scriptwars.game.snakes;

import java.io.IOException;
import java.util.*;

import com.liamtbrand.snake.controller.IStage;
import com.liamtbrand.snake.model.IGameObjectModel.Type;
import com.liamtbrand.snake.model.concrete.Stage;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.Coordinates;
import brownshome.scriptwars.game.Direction;

public class TestAI {
	private static Network network;
	
	private static IStage stage;
	
	public static void main(String[] args) throws IOException {
		int id = 65536;

		network = new Network(id, "localhost", "Test AI");
		Direction direction = Direction.UP;
		System.out.println("Connecting");
		
		while(network.nextTick()) {
			stage = new Stage(new BasicMapModel(network));
			
			ClientSnake[] snakes = new ClientSnake[network.getByte()];
			ClientSnake me = null;
			for(int i = 0; i < snakes.length; i++) {
				snakes[i] = new ClientSnake(network);
				if(snakes[i].getID() == id)
					me = snakes[i];
			}
			
			if(me == null) {
				network.sendByte(0);
				continue;
			}
			
			EnumMap<Type, List<ClientGameObject>> map = new EnumMap<>(Type.class);
			for(Type type : Type.values())
				map.put(type, new ArrayList<>());
			
			int objects = network.getByte();
			for(int i = 0; i < objects; i++) {
				ClientGameObject cgo = new ClientGameObject(network);
				map.get(cgo.type).add(cgo);
			}
			
			Queue<Coordinates> queue = new ArrayDeque<>();
			Map<Coordinates, Direction> route = new HashMap<>();
			queue.add(me.getHead());

			loop:
			while(!queue.isEmpty()) {
				Coordinates coord = queue.remove();

				for(Direction dir : Direction.values()) {
					Coordinates c = dir.move(coord);
					
					if(isValid(c) && !stage.getMap().isWall(c.getX(), c.getY()) && !route.containsKey(c)) {
						Direction getTo = dir;
						if(route.containsKey(coord)) {
							getTo = route.get(coord);
						}

						route.put(c, getTo);
						queue.add(c);

						if(map.get(Type.FOOD).stream().anyMatch(cgo -> cgo.coord.equals(c))) {
							direction = getTo;
							break loop;
						}
					}
				}
			}
			
			network.sendByte(direction.ordinal());
		}
		
		System.out.println(network.getConnectionStatus()); //This function may not be available on some languages.
	}

	private static boolean isValid(Coordinates c) {
		return c.getX() > 0 && c.getY() > 0 && c.getX() < stage.getMap().getWidth() && c.getY() < stage.getMap().getHeight();
	}
	
}
