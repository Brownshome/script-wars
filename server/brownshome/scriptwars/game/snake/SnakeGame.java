package brownshome.scriptwars.game.snake;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.liamtbrand.snake.game.Engine;
import com.liamtbrand.snake.model.concrete.test.TestMap;

import brownshome.scriptwars.connection.ConnectionHandler;
import brownshome.scriptwars.connection.UDPConnectionHandler;
import brownshome.scriptwars.game.BotFunction;
import brownshome.scriptwars.game.DisplayHandler;
import brownshome.scriptwars.game.Game;
import brownshome.scriptwars.game.GameType;
import brownshome.scriptwars.game.Player;

public class SnakeGame extends Game {
	private final Engine engine;
	
	//Used on the website
	public SnakeGame(GameType type) {
		super(type);
		engine = new Engine();
		setupEngine();
	}

	//Used by the judging server
	public SnakeGame(GameType type, int ticks, int timeout) {
		super(type, ticks, timeout);
		engine = new Engine();
		setupEngine();
	}

	private void setupEngine() {
		engine.selectMap(new TestMap());
	}
	
	@Override
	public boolean hasPerPlayerData() {
		return false;
	}

	@Override
	public int getMaximumPlayers() {
		return 8;
	}

	@Override
	public int getTickRate() {
		return 250;
	}

	@Override
	protected void tick() {
		engine.tick();
	}

	public static String getName() {
		return "Snake";
	}

	public static String getDescription() {
		return "A 2D snake game, just like your Nokia used to play.";
	}
	
	public static Map<String, BotFunction> getBotFunctions() {
		//Add Server AI here
		return Collections.emptyMap();
	}
	
	@Override
	public BufferedImage getIcon(Player<?> player, Function<String, File> pathTranslator) throws IOException {
		return ImageIO.read(pathTranslator.apply("icon.png"));
		//You may want to apply some colouring here
	}

	@Override
	public int getDataSize() {
		assert false;
		
		//TODO Build the data protocol with the clients
		
		return 0;
	}

	@Override
	public boolean getData(Player<?> player, ByteBuffer data) {
		assert false;
		
		//TODO Build the data protocol with the clients
		
		return true;
	}

	@Override
	public void processData(ByteBuffer data, Player<?> player) {
		assert false;
		
		//TODO Build the data protocol with the clients
	}

	@Override
	protected void displayGame() {
		assert false;
	}

	@Override
	public ConnectionHandler<?> getPreferedConnectionHandler() {
		return UDPConnectionHandler.instance();
	}

	@Override
	protected DisplayHandler constructDisplayHandler() {
		assert false;
		
		//Something like return new SnakeGameDisplayHandler();
		
		return null;
	}
}
