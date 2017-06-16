package brownshome.scriptwars.game;

public enum BotDifficulty {
	HARD("Hard", "brownshome.scriptwars.game.tanks.HardAI"),
	MEDIUM("Normal", "brownshome.scriptwars.game.tanks.NormalAI"),
	EASY("Easy", "brownshome.scriptwars.game.tanks.NormalAI"),
	RANDOM("Random", "brownshome.scriptwars.game.tanks.RandomAI");
	
	public final String name;
	public final String classString;
	
	private BotDifficulty(String name, String classString) {
		this.name = name;
		this.classString = classString;
	}

	public String getName() {
		return name;
	}

	public String getClassString() {
		return classString;
	}
}
