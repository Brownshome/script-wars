package brownshome.scriptwars.game;

public enum Difficulty {
	TRIVIAL("Trivial"), EASY("Easy"), MEDIUM("Medium"), HARD("Hard"), EXPERT("Expert");

	private String name;
	Difficulty(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
