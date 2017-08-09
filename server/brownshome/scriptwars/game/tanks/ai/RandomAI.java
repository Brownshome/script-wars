package brownshome.scriptwars.game.tanks.ai;

import java.io.IOException;
import java.util.Scanner;

import brownshome.scriptwars.game.tanks.*;

public class RandomAI extends ServerAI {
	RandomAI(String[] args) throws IOException {
		super(args);

		while(api.nextTick()) {
			if(!api.isAlive()) {
				continue;
			}

			// Move randomly, this will be overwritten if we can see someone.
			int direction = (int) (Math.random() * 4);
			api.move(Direction.values()[direction]);

			if(api.getAmmo() == 0)
				continue;
			
			// See if there is a tank in our field of view,
			// and if there is select it.
			Tank targetTank = null;
			for(Tank tank : api.getVisibleTanks()){
				targetTank = tank;
			}

			// If we can see a tank, lets shoot it.
			if(targetTank != null){
				Coordinates targetPosition = targetTank.getPosition();
				Coordinates myPosition = api.getCurrentPosition();

				Direction targetDirection = Direction.getDirection(targetPosition, myPosition);
				if(targetDirection != null) {
					//We have a clear shot on the target
					api.shoot(targetDirection);
				}
			}

			//System.out.println("Position: " + api.getCurrentPosition());
		}

		if(args.length == 0)
			System.out.println("Disconnected from server:\n\t" + api.getConnectionStatus());
	}

	public static void main(String[] args) throws IOException {
		new RandomAI(args);
	}

	@Override
	String name() {
		return "Random AI";
	}
}
