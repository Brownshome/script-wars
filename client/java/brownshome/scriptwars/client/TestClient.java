package brownshome.scriptwars.client;

public class TestClient {
	public static void main(String[] args) throws InterruptedException { 
		Network.connect(args.length == 0 ? 65536 : Integer.parseInt(args[0]), "localhost", "TESTING");

		while(Network.nextTick()) {
			if(Network.getByte() == 0)
				System.out.println("We died");
			
			//pro level strats
			Network.sendByte((byte) (Math.random() * 2 + 1));
			Network.sendByte((byte) (Math.random() * 4));
		}
	}
}
