package brownshome.scriptwars.client;

public class TestClient {
	public static void main(String[] args) throws InterruptedException { 
		Network.connect(65536, "localhost", 35565, "TESTING");

		while(Network.nextTick()) {
			System.out.println("We are slot " + Network.getInt());
		}
	}
}
