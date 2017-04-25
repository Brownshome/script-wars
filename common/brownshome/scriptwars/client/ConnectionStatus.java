package brownshome.scriptwars.client;

public final class ConnectionStatus {
	public static final ConnectionStatus NOT_CONNECTED = new ConnectionStatus("NOT_CONNECTED");
	public static final ConnectionStatus CONNECTED = new ConnectionStatus("CONNECTED");
	public static final ConnectionStatus DROPPED = new ConnectionStatus("DROPPED: The connection was terminated for an unknown reason");
	public static final ConnectionStatus DISCONNECTED = new ConnectionStatus("DISCONNECTED: The connection was terminated by the end of the game");
	public static final ConnectionStatus FAILED_TO_KEEP_UP = new ConnectionStatus("FAILED_TO_KEEP_UP: The connection was terminated because the we couldn't keep up with the game tick");
	
	public static ConnectionStatus ERROR(String msg) {
		return new ConnectionStatus("ERROR: " + msg);
	}
	
	private final String msg;
	
	private ConnectionStatus(String msg) {
		this.msg = msg;
	}
}