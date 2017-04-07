import brownshome.scriptwars.client.tankapi.TankAPI;

public class ExampleTankAI {

    public static void main(String[] args) {

        // args[0] should contain the game id.
        // You can request one from: http://13.55.154.170/games/Tanks

        TankAPI api = new TankAPI(args[0], "13.55.154.170", 35565, "John Smith");

        while(api.nextTick()) {

        	 int direction = (int) (Math.random()*4);
           api.move(direction);

        }
    }
}
