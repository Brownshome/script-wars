package brownshome.scriptwars.game;

import java.util.logging.Level;

import brownshome.scriptwars.connection.MemoryConnectionHandler;
import brownshome.scriptwars.game.tanks.HardAI;
import brownshome.scriptwars.game.tanks.NormalAI;
import brownshome.scriptwars.game.tanks.RandomAI;
import brownshome.scriptwars.server.Server;

public enum BotDifficulty {
	HARD("Hard", HardAI::main),
	MEDIUM("Normal", NormalAI::main),
	EASY("Easy", NormalAI::main),
	RANDOM("Random", RandomAI::main);
	
	@FunctionalInterface
	interface BotFunction {
		void start(String[] args) throws Exception;
	}
	
	private final BotFunction action;
	private final String name;
	
	private BotDifficulty(String name, BotFunction action) {
		this.name = name;
		this.action = action;
	}

	public void start(Game game) throws OutOfIDsException {		
		int ID = game.getID(MemoryConnectionHandler.instance().getProtocolByte());
		
		if(ID == -1)
			throw new OutOfIDsException();
		
		Thread aiThread = new Thread(() -> {
			try {
				action.start(new String[] {String.valueOf(ID)});
			} catch (Exception e) {
				Server.LOG.log(Level.WARNING, "Unable to start " + name, e);
			}
		}, name + "@" + ID + " AI thread");
		
		aiThread.setDaemon(true);
		aiThread.start();
	}
	
	public String getName() {
		return name;
	}
}
