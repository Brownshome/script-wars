package brownshome.scriptwars.standalone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.*;
import java.util.logging.Level;

import brownshome.scriptwars.connection.MemoryConnectionHandler;
import brownshome.scriptwars.game.*;
import brownshome.scriptwars.game.tanks.TankGame;
import brownshome.scriptwars.server.Server;

/**
 * A local judging server that uses the in-memory connect to greatly accelerate the tick rate and speed up the game speeds.
 * @author James
 */
public final class StandaloneJudge {
	public static void main(String[] args) throws IOException, UnknownServerBotException, GameCreationException, InterruptedException {
		Properties settings = new Properties();
		
		Path settingsFile = Paths.get("settings.prop");
		
		if(Files.exists(settingsFile)) {
			try(InputStream stream = Files.newInputStream(settingsFile, StandardOpenOption.CREATE)) {
				settings.load(stream);
			}
		}
		
		StandaloneJudge judge = new StandaloneJudge(settings);
		
		try(OutputStream stream = Files.newOutputStream(settingsFile, StandardOpenOption.CREATE)) {
			settings.store(stream, "Settings file for the judge system.");
		}
		
		Path players = Paths.get("players");
		URLClassLoader classLoader = new URLClassLoader(new URL[] {players.toUri().toURL()});
		
		if(!Files.exists(players))
			Files.createDirectory(players);
		
		Iterable<String> mainClasses = Files.walk(players).filter(Files::isRegularFile).map(p -> {
			Attributes attr;
			try(JarFile file = new JarFile(p.toFile());) {
				attr = file.getManifest().getMainAttributes();
				String main = attr.getValue(Attributes.Name.MAIN_CLASS);

				if(main == null) {
					main = p.getFileName().toString().replaceAll("\\.jar", "");
					Server.LOG.log(Level.WARNING, "Jar " + p.getFileName() + " does not define the main class in it's manifest.");
				}

				return main;
			} catch (IOException e) {
				System.out.println("Invalid JAR file: " + e.getMessage());
				return null;
			}

		}).filter(Objects::nonNull)::iterator;
		
		judge.loadBots(classLoader, mainClasses);
		
		judge.runCompetition();
	}
	
	private final Map<String, Contestant> mapping = new HashMap<>();
	private final GameType type;
	private final int timeout;
	private final int roundsPerLayer;
	private final double layerPercentage;
	private final int peoplePerGame;
	private final Contestant fillerContestant;
	private final int gameLength;
	
	private final class Contestant {
		private final BotFunction function;
		private final String name;
		private AtomicInteger score;
		private List<Thread> threads = new ArrayList<>();
		
		public Contestant(Class<?> clazz) throws NoSuchMethodException {
			name = clazz.getName();
			score = new AtomicInteger();
			
			Method m = clazz.getMethod("main", String[].class);
			function = s -> m.invoke(null, (Object) s);
		}
		
		public Contestant(BotFunction serverBot) {
			function = serverBot;
			score = new AtomicInteger();
			name = "filler bot";
		}

		public void join(Game game) {
			int id = game.getID(MemoryConnectionHandler.instance().getProtocolByte());
			
			Thread aiThread = new Thread(() -> {
				try {
					function.start(new String[] {Integer.toUnsignedString(id)});
				} catch (Exception e) {
					Server.LOG.log(Level.WARNING, "Error in server bot " + name, e);
				}
				
				score.addAndGet(game.getPlayer(id & 0xff).getScore());
			}, name + "@" + id + " AI thread");
			aiThread.start();
			
			threads.add(aiThread);
		}

		public void terminate() {
			for(Thread t : threads) {
				if(t.isAlive())
					t.stop(); //We have to use stop here to cause broken bots to die. It should be thread safe as the only shared state is the command queue, which will have already been killed from the server side.
			}
		}
		
		@Override
		public String toString() {
			return name + ": " + score;
		}
	}
	
	private StandaloneJudge(Properties settings) throws UnknownServerBotException {
		Server.initialize();
		
		type = GameType.getGameType(settings.getProperty("game", TankGame.getName()));
		timeout = Integer.parseInt(settings.getProperty("timeout", "50"));
		roundsPerLayer = Integer.parseInt(settings.getProperty("roundsPerLayer", "100"));
		layerPercentage = Double.parseDouble(settings.getProperty("layerPercentage", "0.5"));
		gameLength = Integer.parseInt(settings.getProperty("gameLength", "5000"));
		peoplePerGame = Integer.parseInt(settings.getProperty("peoplePerGame", "8"));
		String fillerBot = settings.getProperty("fillerBot", "Simple AI");
		fillerContestant = new Contestant(type.getServerBot(fillerBot));
	}

	private void loadBots(ClassLoader loader, Iterable<String> mainClasses) {
		for(String mainClass : mainClasses) {
			try {
				mapping.put(mainClass, new Contestant(loader.loadClass(mainClass)));
			} catch (NoSuchMethodException | ClassNotFoundException e) {
				Server.LOG.log(Level.WARNING, "Bot " + mainClass + " failed to load " + e.getMessage());
			}
		}
	}
	
	private void runCompetition() throws GameCreationException, InterruptedException {
		List<Contestant> alive = new ArrayList<>(mapping.values());
		
		while(alive.size() > 1) {
			int numberOfGames = (alive.size() - 1) / peoplePerGame + 1;
			Game[] games = new Game[numberOfGames];
			
			for(int r = 0; r < roundsPerLayer; r++) {
				System.out.println("Round " + r);
				
				for(int i = 0; i < numberOfGames; i++) {
					games[i] = type.createJudgingGame(gameLength, timeout);
				}

				for(int i = 0; i < alive.size(); i++) {
					alive.get(i).join(games[i / peoplePerGame]);
				}

				for(int i = 0; i < peoplePerGame * games.length - alive.size(); i++) {
					fillerContestant.join(games[games.length - 1]);
				}

				Thread.sleep(100);

				List<Thread> gameThreads = new ArrayList<>();
				for(Game game : games) {
					gameThreads.add(game.start());
				}

				//Wait for games to finish
				for(Thread thread : gameThreads) {
					thread.join();
				}

				//Give clients time to close of their own accord, then murder them. This should given them time to find their scores.
				Thread.sleep(100);

				//All contestants should be finished
				for(Contestant c : alive) {
					c.terminate();
				}
			}
			
			//Sort in ascending order
			alive.sort((a, b) -> b.score.get() - a.score.get());
			
			int cutoff = Math.max(1, (int) (layerPercentage * (alive.size() - 1))); //Ensure that there is at least one item left in the competition.
			alive.subList(cutoff, alive.size()).clear();
		}
		
		List<Contestant> contestants = new ArrayList<>(mapping.values());
		contestants.sort((a, b) -> b.score.get() - a.score.get());
		System.out.println(contestants);
	}
}
