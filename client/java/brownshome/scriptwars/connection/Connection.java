package brownshome.scriptwars.connection;

import java.nio.ByteBuffer;

interface Connection {
	void sendData(ByteBuffer data) throws ConnectionException;
	void putHeader(ByteBuffer putInt);
	ByteBuffer waitForData() throws ConnectionException;
}