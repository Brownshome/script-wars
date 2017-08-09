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
import java.util.stream.Collectors;

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
		} else {
			settings.setProperty("game", TankGame.getName());
			settings.setProperty("timeout", "50");
			settings.setProperty("rounds", "10");
			settings.setProperty("gameLength", "10000");
			settings.setProperty("peoplePerGame", "8");
			settings.setProperty("fillerBot", "Simple AI");
			
			try(OutputStream stream = Files.newOutputStream(settingsFile, StandardOpenOption.CREATE)) {
				settings.store(stream, "Settings file for the judge system.");
			}
		}
		
		StandaloneJudge judge = new StandaloneJudge(settings);
		
		Path players = Paths.get("players");
		
		if(!Files.exists(players)) {
			Files.createDirectory(players);
		}
		
		List<Path> paths = Files.walk(players).filter(Files::isRegularFile).collect(Collectors.toList());
		
		URLClassLoader classLoader = new URLClassLoader(paths.stream().map(p -> {
			try {
				return p.toUri().toURL();
			} catch (MalformedURLException e1) { throw new RuntimeException(e1); }
		}).toArray(URL[]::new));
		
		if(!Files.exists(players))
			Files.createDirectory(players);
		
		
		Iterable<String> mainClasses = paths.stream().map(p -> {
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
				System.err.println("Invalid JAR file: " + e.getMessage());
				return null;
			}

		}).filter(Objects::nonNull)::iterator;
		
		judge.loadBots(classLoader, mainClasses);
		
		judge.runCompetition();
	}
	
	private final Map<String, Contestant> mapping = new HashMap<>();
	private final GameType type;
	private final int timeout;
	private final int rounds;
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
					Server.LOG.log(Level.WARNING, "Error in bot " + name, e);
				}
				
				score.addAndGet(game.getPlayer(id & 0xff).getScore());
			}, name + "@" + id + " AI thread");
			
			aiThread.setDaemon(true);
			aiThread.start();
			
			threads.add(aiThread);
		}

		public void terminate(long limit) {
			for(Thread t : threads) {
				long k;
				try {
					if((k = limit - System.currentTimeMillis()) > 0)
						t.join(k);
					
					if(t.isAlive())
						t.stop();
				} catch (InterruptedException e) {}
			}
			
			threads.clear();
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
		rounds = Integer.parseInt(settings.getProperty("rounds", "10"));
		gameLength = Integer.parseInt(settings.getProperty("gameLength", "10000"));
		peoplePerGame = Integer.parseInt(settings.getProperty("peoplePerGame", "8"));
		String fillerBot = settings.getProperty("fillerBot", "Simple AI");
		fillerContestant = new Contestant(type.getServerBot(fillerBot));
	}

	private void loadBots(ClassLoader loader, Iterable<String> mainClasses) {
		for(String mainClass : mainClasses) {
			try {
				mapping.put(mainClass, new Contestant(loader.loadClass(mainClass)));
			} catch (NoSuchMethodException | ClassNotFoundException e) {
				Server.LOG.log(Level.WARNING, "Bot " + mainClass + " failed to load:\n" + e.toString());
			}
		}
	}
	
	private void runCompetition() throws GameCreationException, InterruptedException {
		List<Contestant> contestants = new ArrayList<>(mapping.values());

		int numberOfGames = (contestants.size() - 1) / peoplePerGame + 1;
		Game[] games = new Game[numberOfGames];

		for(int r = 0; r < rounds; r++) {
			System.err.println("Round " + r);

			for(int i = 0; i < numberOfGames; i++) {
				games[i] = type.createJudgingGame(gameLength, timeout);
			}

			for(int i = 0; i < contestants.size(); i++) {
				contestants.get(i).join(games[i % games.length]);
			}

			//Now fill in filler contestants from the back
			for(int i = 0; i < peoplePerGame * games.length - contestants.size(); i++) {
				fillerContestant.join(games[games.length - 1 - i % games.length]);
			}

			List<Thread> gameThreads = new ArrayList<>();
			for(Game game : games) {
				gameThreads.add(game.startWhenReady(100));
			}

			//Wait for games to finish
			for(Thread thread : gameThreads) {
				thread.join();
			}

			long now = System.currentTimeMillis();

			//All contestants should be finished
			for(Contestant c : contestants) {
				c.terminate(now + 100);
			}
			
			Collections.shuffle(contestants);
		}
		
		contestants.sort((a, b) -> b.score.get() - a.score.get());
		System.err.println(contestants);
	}
}
