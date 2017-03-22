package brownshome.scriptwars.server.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	
	Game availableGame;
	
	public GameType(Class<? extends Game> clazz) throws GameCreationException {
		Constructor<? extends Game> constructor;
		
		try {
			constructor = clazz.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GameCreationException("Game " + clazz.getSimpleName() + " did not have a suitable constructor.", e);
		}
		
		this.constructor = () -> {
			try {
				Game game = constructor.newInstance();
				game.type = this;
				game.start();
				return game;
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
	
	public String getName() {
		return name;
	}

	/** generates a new ID 
	 * @throws GameCreationException if a new game could not be created and the existing one is full */
	public int getUserID() throws GameCreationException {
		return getAvailableGame().getConnectionHandler().getID();
	}
	
	public Game getAvailableGame() throws GameCreationException {
		if(availableGame == null || !availableGame.hasSpaceForPlayer()) {
			availableGame = constructor.get();
		}
		
		return availableGame;
	}
}
