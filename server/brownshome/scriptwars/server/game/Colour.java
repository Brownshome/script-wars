package brownshome.scriptwars.server.game;

public enum Colour {
	RED, GREEN, CYAN, BLUE, VIOLET, PINK, YELLOW, BLACK, INDIGO, ORANGE;

	public static Colour translateToColour(Object object) {
		return values()[(object.hashCode() & 0x7fffffff) % values().length];
	}
}
