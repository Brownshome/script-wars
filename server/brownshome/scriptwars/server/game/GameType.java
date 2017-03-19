package brownshome.scriptwars.server.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

import brownshome.scriptwars.server.game.tanks.TankGame;

public class GameType {
	static interface GameCreator {
		Game get() throws GameCreationException;
	}
	
	static Map<String, GameType> publicGames = new HashMap<>();
	static Map<String, GameType> debugGames = new HashMap<>();
	
	public static void addType(Class<TankGame> clazz) throws GameCreationException {
		GameType type = new GameType(clazz);
		publicGames.put(type.getName(), type);
	}
	
	public static Collection<GameType> getGameTypes() {
		return publicGames.values();
	}
	
	public static void addDebugType(Class<TestGame> clazz) throws GameCreationException {
		GameType type = new GameType(clazz);
		debugGames.put(type.getName(), type);
	}
	
	public static GameType getGameType(String string) {
		return publicGames.get(string);
	}
	
	GameCreator constructor;
	String name;
	String description;
	
	//tmp variable
	Game game;
	
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
	
	public int getId() {
		if(game == null) {
			try {
				game = constructor.get();
				game.start();
			} catch (GameCreationException e) {}
		}
		
		return game.slot;
	}
}
