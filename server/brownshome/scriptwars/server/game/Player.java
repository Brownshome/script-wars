package brownshome.scriptwars.server.game;

/** Holds the identifying information for each connected member.
 * This class is suitable for using as a key in a Map */
public class Player {
	String name;
	boolean isActive = false;
	int slot;
	
	public Player(int slot) {
		this.slot = slot;
		this.name = "Player-" + slot;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public int getSlot() {
		return slot;
	}

	public void setName(String name) {
		this.name = name;
	}
}
