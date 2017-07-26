package brownshome.scriptwars.game.tanks.ai;

import java.io.IOException;

//Just runs to the nearest ammo pack and away from bullets and other such thing
public class ScaredAI extends ServerAI {
	ScaredAI(String[] args) throws IOException {
		super(args);
		
		while(api.nextTick()) {
			if(!api.isAlive())
				continue;
			
			pathTo(api.getMap().getAmmoPickups()::contains);
			avoidEventualBullets();
			avoidDirectBullets();
		}
	}

	public static void main(String[] args) throws IOException {
		new ScaredAI(args);
	}

	@Override
	String name() {
		return "Scared AI";
	}

}
