package brownshome.scriptwars.game.tanks;

import brownshome.scriptwars.game.Player;

public class TankStats {
	private int kills = 0;
	private int deaths = 0;
	private int shotsFired = 0;
	private int ammoPickedUp = 0;
	private int movesMade = 0;
	private int movesFailed = 0;
	private Player<?> player;
	
	public TankStats(Player<?> player) {
		this.player = player;
	}
	
	public void kill() { 
		kills++; 
		player.setScore(player.getScore() + 1);
	}
	
	public void death() { 
		deaths++; 
		if(player.getScore() > 0) {
			player.setScore(player.getScore() - 1);
		}
	}
	
	public void shotFired() { shotsFired++; }
	public void ammoPickedUp() { ammoPickedUp++; }
	public void move() { movesMade++; }
	public void failedMove() { movesFailed++; }
	
	public int getKills() { return kills; }
	public int getDeaths() { return deaths; }
	public int getShotsFired() { return shotsFired; }
	public int getAmmoPickedUp() { return ammoPickedUp; }
	public int getMovesMade() { return movesMade; }
	public int getMovesFailed() { return movesFailed++; }
}
