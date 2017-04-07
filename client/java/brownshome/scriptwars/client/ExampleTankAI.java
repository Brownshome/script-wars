package brownshome.scriptwars.client;

import brownshome.scriptwars.client.tankapi.TankAPI;
import brownshome.scriptwars.client.tankapi.TankAPI.Direction;
import brownshome.scriptwars.client.tankapi.TankAPI.GameMap;
import brownshome.scriptwars.client.tankapi.TankAPI.Tank;

public class ExampleTankAI {

	public static void main(String[] args) {

		// args[0] should contain the game id.
		// You can request one from: http://13.55.154.170/games/Tanks
		
		//int id = Integer.valueOf(args[0]);

		TankAPI api = new TankAPI(65559, "13.55.154.170", 35565, "John Smith");

		while(api.nextTick()) {

			int direction = (int) (Math.random()*4);
			api.move(TankAPI.Direction.values()[direction]);
			
			// See if there is a tank in our field of view,
			// and if there is select it.
			Tank targetTank = null;
			for(Tank tank : api.getVisibleTanks()){
				targetTank = tank;
			}
			
			// TODO maybe some path finding?
			GameMap map = api.getMap();
			
			// If we can see a tank, lets shoot it.
			if(targetTank != null){
				if(api.me().getCoordinates().getX() == targetTank.getCoordinates().getX()){
					if(api.me().getCoordinates().getY() > targetTank.getCoordinates().getY()){
						api.shoot(Direction.DOWN);
					}else{
						api.shoot(Direction.UP);
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
		}
	}
}
