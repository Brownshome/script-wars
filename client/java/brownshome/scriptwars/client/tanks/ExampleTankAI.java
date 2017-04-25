package brownshome.scriptwars.client.tanks;

import java.io.IOException;

import brownshome.scriptwars.game.tanks.*;

public class ExampleTankAI {
	/**
	 * The main method to start the AI and connect to the server.
	 * 
	 * args[0] should contain the game id.
	 * You can request one from: http://script-wars.com/games/Tanks
	 * by clicking the 'Join' button.
	 * 
	 * @param args The input arguments containing the ID allocated by the server
	 * @throws IOException If we failed to connect to the server
	 */
	public static void main(String[] args) throws IOException {
		// args[0] should contain the game id.
		// You can request one from: http://script-wars.com/games/Tanks
		// by clicking the 'Join' button
		
		int id;
		if(args.length > 0){
			id = Integer.valueOf(args[0]);
		} else {
			System.out.println("Usage: JAVACOMMAND serverid");
			System.exit(1);
			return;
		}

		TankAPI api = new TankAPI(id, "www.script-wars.com", "John Smith");

		while(api.nextTick()) {

			// Move randomly, this will be overwritten if we can see someone.
			int direction = (int) (Math.random()*4);
			api.move(Direction.values()[direction]);
			
			// See if there is a tank in our field of view,
			// and if there is select it.
			Tank targetTank = null;
			for(Tank tank : api.getVisibleTanks()){
				targetTank = tank;
			}
			
			// TODO maybe some path finding?
			//TankAPI.Map map = api.getMap();
			
			// If we can see a tank, lets shoot it.
			if(targetTank != null){
				Coordinates targetPosition = targetTank.getPosition();
				Coordinates myPosition = api.me().getPosition();
				
				Direction targetDirection = Direction.getDirection(targetPosition, myPosition);
				if(targetDirection != null) {
					//Impossible for targetDirection to be null in the current rule-set but just in case.
					api.shoot(targetDirection);
				}
			}
			
			System.out.println("Position: " + api.me().getPosition());
		}
		
		System.out.println("Disconnected from server:\n\t" + api.getConnectionStatus());
	}
}
