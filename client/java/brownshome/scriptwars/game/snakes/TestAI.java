package brownshome.scriptwars.game.snakes;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.stream.Stream;

import com.liamtbrand.snake.controller.IStage;
import com.liamtbrand.snake.model.IGameObjectModel.Type;
import com.liamtbrand.snake.model.ISnakeModel.Direction;
import com.liamtbrand.snake.model.concrete.Stage;

import brownshome.scriptwars.connection.Network;
import brownshome.scriptwars.game.Coordinates;

public class TestAI {
	private static Network network;

	private static IStage stage;

	public static void main(String[] args) throws IOException {
		int id;

		if(args.length == 0) {
			System.out.print("ID: ");
			id = (int) new Scanner(System.in).nextLong();
		} else {
			id = Integer.parseInt(args[0]);
		}

		network = new Network(id, "localhost", "Test AI");
		System.out.println("Connecting");
		Direction direction = Direction.NORTH;

		while(network.nextTick()) {
			stage = new Stage(new BasicMapModel(network));

			ClientSnake[] snakes = new ClientSnake[network.getByte()];
			ClientSnake me = null;
			for(int i = 0; i < snakes.length; i++) {
				snakes[i] = new ClientSnake(network);
				if(snakes[i].getID() == id)
					me = snakes[i];
			}

			EnumMap<Type, List<ClientGameObject>> map = new EnumMap<>(Type.class);
			for(Type type : Type.values())
				map.put(type, new ArrayList<>());

			int objects = network.getByte();
			for(int i = 0; i < objects; i++) {
				ClientGameObject cgo = new ClientGameObject(network);
				map.get(cgo.type).add(cgo);
			}

			if(me != null) {
				System.out.println(me.getHead());
			}

			Queue<Coordinates> queue = new ArrayDeque<>();
			Map<Coordinates, Direction> route = new HashMap<>();
			if(me != null) {
				queue.add(me.getHead());
				for(Direction dir : Direction.values()) {
					Coordinates c = wrap(new Coordinates(me.getHead().getX() + dir.dx(), me.getHead().getY() + dir.dy()));

					if(!stage.getMap().isWall(c.getX(), c.getY()) && !route.containsKey(c) && 
							!Arrays.stream(snakes).flatMap(snake -> Stream.generate(snake.iterator()::next).limit(snake.getLength())).anyMatch(cx -> cx.equals(c))
							) {
						direction = dir;
					}
				}

				loop: while(!queue.isEmpty()) {
					Coordinates coord = queue.remove();

					for(Direction dir : Direction.values()) {
						Coordinates c = wrap(new Coordinates(coord.getX() + dir.dx(), coord.getY() + dir.dy()));

						if(!stage.getMap().isWall(c.getX(), c.getY()) && !route.containsKey(c) && 
								!Arrays.stream(snakes).flatMap(snake -> Stream.generate(snake.iterator()::next).limit(snake.getLength())).anyMatch(cx -> cx.equals(c))
								) {


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
			} else {
				System.out.println("No snake, we died?");
			}

			network.sendByte(direction.ordinal());
		}

		System.out.println(network.getConnectionStatus()); //This function may not be available on some languages.
	}

	private static Coordinates wrap(Coordinates c) {
		return new Coordinates((c.getX() + stage.getMap().getWidth()) % stage.getMap().getWidth(), (c.getY() + stage.getMap().getHeight()) % stage.getMap().getHeight());
	}
}
