package brownshome.scriptwars.connection;

public class ConnectionException extends Exception {
	ConnectionStatus connectionStatus;
	
	public ConnectionException(ConnectionStatus connectionStatus, Throwable t) {
		super(t);
		
		this.connectionStatus = connectionStatus;
	}
	
	public ConnectionException(ConnectionStatus connectionStatus) {
		this.connectionStatus = connectionStatus;
	}

	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}
}
