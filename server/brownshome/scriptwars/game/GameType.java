package brownshome.scriptwars.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameType {
	static interface GameCreator {
		Game get() throws GameCreationException;
	}
	
	static interface JudgeGameCreator {
		Game get(int ticks, int timeout) throws GameCreationException;
	}
	
	private static Map<String, GameType> publicGames = new HashMap<>();
	
	public static void addType(Class<? extends Game> clazz, Difficulty difficulty) throws GameCreationException {
		GameType type = new GameType(clazz, difficulty);
		publicGames.put(type.getName(), type);
	}
	
	public static void addBetaType(Class<? extends Game> clazz, Difficulty difficulty) throws GameCreationException {
		GameType type = new GameType(clazz, true, Language.ANY, difficulty);
		publicGames.put(type.getName(), type);
	}
	
	public static Collection<GameType> getGameTypes() {
		return publicGames.values();
	}
	
	public static GameType getGameType(String string) {
		return publicGames.get(string);
	}
	
	private GameCreator constructor;
	private JudgeGameCreator judgeConstructor;
	private String name;
	private String description;
	private boolean isBetaGame;
	private Language language;
	private Difficulty difficulty;
	private Map<String, BotFunction> serverBots;
	
	private ReentrantReadWriteLock gamesLock = new ReentrantReadWriteLock();
	private Collection<Game> games = new ArrayList<>();
	private Set<Runnable> onListUpdate = new HashSet<>();
	
	public GameType(Class<? extends Game> clazz, Difficulty difficulty) throws GameCreationException {
		this(clazz, false, Language.ANY, difficulty);
	}
	
	@SuppressWarnings("unchecked")
	public GameType(Class<? extends Game> clazz, boolean isBeta, Language language, Difficulty difficulty) throws GameCreationException {
		this.isBetaGame = isBeta;
		this.difficulty = difficulty;
		this.language = language;
		
		Constructor<? extends Game> constructor;
		
		try {
			constructor = clazz.getConstructor(GameType.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GameCreationException("Game " + clazz.getSimpleName() + " did not have a suitable constructor.", e);
		}
		
		this.constructor = () -> {
			try {
				Game.getActiveGamesLock().writeLock().lock();
				Game game = constructor.newInstance(this);
				game.addToSlot();
				return game;
			} catch (OutOfIDsException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new GameCreationException("Unable to instantiate game", e);
			} finally {
				Game.getActiveGamesLock().writeLock().unlock(); //This will always be executed, even if the function returns normally
			}
		};
		
		Constructor<? extends Game> judgeConstructor;
		
		try {
			judgeConstructor = clazz.getConstructor(GameType.class, Integer.TYPE, Integer.TYPE);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GameCreationException("Game " + clazz.getSimpleName() + " did not have a suitable constructor.", e);
		}
		
		this.judgeConstructor = (ticks, timeout) -> {
			try {
				Game.getActiveGamesLock().writeLock().lock();
				Game game = judgeConstructor.newInstance(this, ticks, timeout);
				game.addToSlot();
				return game;
			} catch (OutOfIDsException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new GameCreationException("Unable to instantiate game", e);
			} finally {
				Game.getActiveGamesLock().writeLock().unlock(); //This will always be executed, even if the function returns normally
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
		
		try {
			serverBots = (Map<String, BotFunction>) clazz.getMethod("getBotFunctions").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new GameCreationException("Game " + clazz.getSimpleName() + " did not define \'static Map<String, BotFunction> getBotFunctions()\'.", e);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getPlayerCount() {
		gamesLock.readLock().lock();
		try {
			return games.stream().mapToInt(Game::getPlayerCount).sum();
		} finally {
			gamesLock.readLock().unlock();
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	/** generates a new ID 
	 * @throws GameCreationException if a new game could not be created and the existing one is full */
	public int getUserID() throws GameCreationException {
		return getAvailableGame().getID();
	}
	
	/** generates a new ID 
	 * @throws GameCreationException if a new game could not be created and the existing one is full */
	public int getUserID(int protocol) throws GameCreationException {
		return getAvailableGame().getID(protocol);
	}
	
	public void endGame(Game game) {
		gamesLock.writeLock().lock();
		games.remove(game);
		gamesLock.writeLock().unlock();
	}
	
	public Game getAvailableGame() throws GameCreationException {
		gamesLock.writeLock().lock();
		try {
			for(Game game : games) {
				if(game.isSpaceForPlayer())
					return game;
			}

			Game availableGame = constructor.get();
			games.add(availableGame);
			signalListUpdate();
			return availableGame;
		} finally {
			gamesLock.writeLock().unlock();
		}
	}
	
	public Game createJudgingGame(int ticks, int timeout) throws GameCreationException {
		gamesLock.writeLock().lock();
		try {
			Game availableGame = judgeConstructor.get(ticks, timeout);
			games.add(availableGame);
			signalListUpdate();
			return availableGame;
		} finally {
			gamesLock.writeLock().unlock();
		}
	}
	
	//Can be called from the tomcat thread
	public Collection<Game> getGames() {
		List<Game> list;
		
		try {
			gamesLock.readLock().lock();
			list = new ArrayList<>(games);
		} finally {
			gamesLock.readLock().unlock();
		}
			
		list.removeIf(g -> g.getActivePlayers().isEmpty());
		return list;
	}

	/** Updates the game table */
	public synchronized void signalListUpdate() {
		DisplayHandler.sendGameTableUpdate();
	}

	public String getDifficulty() {
		return difficulty.getName();
	}
	
	public String getLanguage() {
		return language.getName();
	}
	
	public boolean isBetaGame() {
		return isBetaGame;
	}

	/** Returns the main function of the requested bot, throwing an IllegalArgumentException if there is none */
	public BotFunction getServerBot(String name) throws UnknownServerBotException {
		BotFunction function = serverBots.get(name);
		if(function == null)
			throw new UnknownServerBotException(name);
		
		return function;
	}
	
	public List<String> getDifficulties() {
		List<String> result = new ArrayList<>(serverBots.keySet());
		result.sort(null);
		return result;
	}
}
