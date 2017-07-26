package brownshome.scriptwars.game;

public class UnknownServerBotException extends Exception {
	public UnknownServerBotException(String name) {
		super("Bot " + name + " is not a valid bot");
	}
}
