package brownshome.scriptwars.client;

public class TestClient {
	public static void main(String[] args) throws InterruptedException { 
		Network.connect(args.length == 0 ? 65536 : Integer.parseInt(args[0]), "localhost", 35565, "TESTING");

		while(Network.nextTick() && Network.getByte() != 0) {		
			//pro level strats
			Network.sendByte((byte) (Math.random() * 2 + 1));
			Network.sendByte((byte) (Math.random() * 4));
		}
	}
}
