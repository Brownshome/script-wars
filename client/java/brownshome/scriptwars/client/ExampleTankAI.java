package brownshome.scriptwars.client;

import brownshome.scriptwars.client.tankapi.TankAPI;
import brownshome.scriptwars.client.tankapi.TankAPI.Tank;
import brownshome.scriptwars.server.game.tanks.Direction;

public class ExampleTankAI {

	public static void main(String[] args) {

		// args[0] should contain the game id.
		// You can request one from: http://13.55.154.170/games/Tanks
		
		int id = 65760; // Default id if none is passed (for testing)
		if(args.length > 0){
			id = Integer.valueOf(args[0]);
		}

		TankAPI api = new TankAPI(id, "13.55.154.170", "John Smith");

		while(api.nextTick() == TankAPI.ConnectionStatus.CONNECTED) {

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
				if(api.me().getCoordinates().getX() == targetTank.getCoordinates().getX()){
					if(api.me().getCoordinates().getY() > targetTank.getCoordinates().getY()){
						api.shoot(Direction.UP);
					}else{
						api.shoot(Direction.DOWN);
					}
				}
				if(api.me().getCoordinates().getY() == targetTank.getCoordinates().getY()){
					if(api.me().getCoordinates().getX() > targetTank.getCoordinates().getX()){
						api.shoot(Direction.LEFT);
					}else{
						api.shoot(Direction.RIGHT);
					}
				}
			}
			
			System.out.println("Position: "+api.me().getCoordinates().getX()+","+api.me().getCoordinates().getY());
		}
	}
}
