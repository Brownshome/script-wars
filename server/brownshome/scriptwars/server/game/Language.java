package brownshome.scriptwars.server.game;

public enum Language {
	ANY("Any"), JAVA("Java");
	
	private String name;
	Language(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
