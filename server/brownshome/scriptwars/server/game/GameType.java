package brownshome.scriptwars.server.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import brownshome.scriptwars.server.game.tanks.TankGame;

public class GameType {
	static interface GameCreator {
		Game get() throws GameCreationException;
	}
	
	static List<GameType> publicGames = new ArrayList<>();
	static List<GameType> debugGames = new ArrayList<>();
	
	public static void addType(Class<TankGame> clazz) throws GameCreationException {
		publicGames.add(new GameType(clazz));
	}
	
	public static List<GameType> getGameTypes() {
		return publicGames;
	}
	
	public static void addDebugType(Class<TestGame> clazz) throws GameCreationException {
		debugGames.add(new GameType(clazz));
	}
	
	GameCreator constructor;
	String name;
	String description;
	
	public GameType(Class<? extends Game> clazz) throws GameCreationException {
		Constructor<? extends Game> constructor;
		
		try {
			constructor = clazz.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GameCreationException("Game " + clazz.getSimpleName() + " did not have a suitable constructor.", e);
		}
		
		this.constructor = () -> {
			try {
				return constructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new GameCreationException("Unable to instantiate game", e);
			}
		};
		
		try {
			name = (String) clazz.getMethod("getName").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new GameCreationException("Game " + clazz.getSimpleName() + " did not define \'static String getName()\'.", e);
		}
		
		try {
			description = (String) clazz.getMethod("getDescription").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new GameCreationException("Game " + clazz.getSimpleName() + " did not define \'static String getDescription()\'.", e);
		}
	}
	
	public GameType(Supplier<Game> constructor, String name, String description) {
		this.constructor = constructor::get;
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
}
