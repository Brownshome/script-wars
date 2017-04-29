package brownshome.scriptwars.client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

import brownshome.scriptwars.connection.COBSChannel;

/**
 * The main class that clients can use to communicate with the server.
 * 
 * This class has an internal data buffer of 1024 bytes. Attempts to write more
 * data than that will lead to errors.
 *  
 * Example Usage:
 * <pre>
 * <code>
 * public static main(String[] args) { 
 *  	Network.connect(ID, "52.65.69.217", 35565, "John Smith");
 *  
 *  	while(Network.nextTick()) {
 *  		//Read data using Network.getX();
 *  
 *  		//Send data using Network.sendX();
 *  	}
 * }
 *  </code>
 *  </pre>
 *  
 **/
public class Network {
	static final int MAX_OUTPUT_SIZE = 1024;
	
	private Connection connection;
	private ConnectionStatus connectionStatus = ConnectionStatus.NOT_CONNECTED;
	private ByteBuffer dataOut = ByteBuffer.wrap(new byte[MAX_OUTPUT_SIZE]);
	private ByteBuffer dataIn;
	private int ID = -1;
	
	//Bit packing variables
	private int bit = 0x100;

	private int positionOfByte;

	/**
	 * Call this using the ID given to you by the website to connect
	 * @param ID The ID given to you by the website
	 * @param ip The ip of the website
	 * @param name The name of your bot
	 * @throws IOException If for some reason the site cannot be reached
	 */
	public Network(int ID, String ip, String name) throws IOException {
		this.ID = ID;
		
		dataOut.clear();
		dataOut.putInt(ID);
		sendString(name);
		
		int protocol = ID >> 16 & 0xff;
		
		switch(protocol) {
			case 1:
				connection = new UDPConnection(InetAddress.getByName(ip), 35565);
				break;
			case 2:
				connection = new TCPConnection(InetAddress.getByName(ip), 35566);
				break;
			default:
				throw new IllegalArgumentException("Invalid ID");
		}
	}
	
	/** Waits until all the players have made their moves and sends the data and retrieved a new set of data
	 * from the server. This method returns false if the game is over or you have timed out. 
	 * @return False if the client was disconnected or timed out. The exact cause can be found by calling
	 *         {@link #getConnectionStatus}. Once this method returns false please make attempt to re-use the
	 *         connection.
	 **/
	public boolean nextTick() {
		try {
			connectionStatus = ConnectionStatus.CONNECTED;
			dataOut.flip();
			connection.sendData(dataOut);
			dataOut.clear();
			connection.putHeader(dataOut);
			dataIn = connection.waitForData();
		} catch(ConnectionException ce) {
			connectionStatus = ce.getConnectionStatus();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if the next getX() will throw an exception. Note that even though
	 * this method returns false {@link #getBoolean()} may not error if there are booleans left
	 * in the buffer.
	 * @return true if there is at least one byte left to be read
	 */
	public boolean hasData() {
		return dataIn.hasRemaining();
	}
	
	/**
	 * Queries the connection status. Anything other than {@link ConnectionStatus#CONNECTED}
	 * means that we are not connected to the server.
	 * 
	 * @return The connection status.
	 */
	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}
	
	/**
	 * Gets a single integer from the data.
	 * @return An integer from the data
	 */
	public int getInt() {
		return dataIn.getInt();
	}
	
	/**
	 * Gets a single byte from the data. This byte is returned as an integer in the
	 * range 0-255
	 * @return An integer containing the read byte.
	 */
	public int getByte() {
		return dataIn.get();
	}
	
	/** Gets a true or false value from the data. 
	 * @return A boolean read from the data.
	 **/
	public boolean getBoolean() {
		if(bit == 0x100 || positionOfByte != dataIn.position() - 1) {
			bit = 1;
			positionOfByte = dataIn.position();
			dataIn.get();
		}
		
		int currentByte = dataIn.get(positionOfByte);
		boolean bool = (currentByte & bit) != 0;
		bit <<= 1;
		
		return bool;
	}
	
	/** Gets a string from the data.
	 * @return The decoded String object
	 **/
	public String getString() {
		int length = dataIn.getShort();
		String result = new String(dataIn.array(), dataIn.arrayOffset() + dataIn.position(), length, StandardCharsets.UTF_8);
		dataIn.position(dataIn.position() + length);
		
		return result;
	}
	
	/** 
	 * Gets the raw packet data, Only use this if you know what you are doing. Note that any calls to 
	 * getX() will update the position pointer in the buffer.
	 * @return The ByteBuffer containing the raw data sent by the server.
	 **/
	public ByteBuffer getData() {
		return dataIn;
	}
	
	/** Gets a floating point number from the data 
	 * @return The decoded floating point value 
	 **/
	public float getFloat() {
		return dataIn.getFloat();
	}
	
	/**
	 * Sends a single integer to the server.
	 * @param i The integer to send.
	 **/
	public void sendInt(int i) { 
		dataOut.putInt(i); 
	}
	
	/**
	 * Sends a single byte to the server. This byte is send as a value from 0-255. If the
	 * integer contains values larger that that the lowest 8 bits will be taken.
	 * @param i An integer containing a value from 0-255.
	 */
	public void sendByte(int i) { dataOut.put((byte) (i & 0xff)); }
	
	/**
	 * Sends a single float to the server.
	 * @param f The floating point value
	 */
	public void sendFloat(float f) { dataOut.putFloat(f); }
	
	/** Sends raw data to the server. Only use this if you know what you are doing.
	 * Ensure that the limit and position are set properly.
	 * @param data A byte array holding the data to send.
	 **/
	public void sendData(byte[] data) { 
		dataOut.put(data); 
	}
	
	/**
	 * Sends a String to the server.
	 * @param s The string to send to the server.
	 */
	public void sendString(String s) { 
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		dataOut.putShort((short) bytes.length);
		dataOut.put(bytes);
	}
	
	class UDPConnection implements Connection {
		ByteBuffer buffer = ByteBuffer.allocate(1024); //If you need any larger than this use TCP
		DatagramSocket socket;
		
		UDPConnection(InetAddress ip, int port) throws IOException {
			socket = new DatagramSocket();
			socket.connect(new InetSocketAddress(ip, port));
			socket.setSoTimeout(1500);
		}
		
		@Override
		public void sendData(ByteBuffer data) throws ConnectionException {
			try {
				socket.send(new DatagramPacket(data.array(), data.arrayOffset() + data.position(), data.remaining(), socket.getRemoteSocketAddress()));
			} catch (IOException e) {
				throw new ConnectionException(ConnectionStatus.DROPPED, e);
			}
		}

		@Override
		public ByteBuffer waitForData() throws ConnectionException {
			try {
				buffer.clear();
				DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity());
				socket.receive(packet);
				buffer.position(packet.getOffset());
				buffer.limit(packet.getLength() + packet.getOffset());
			} catch (IOException e) {
				throw new ConnectionException(ConnectionStatus.DROPPED, e);
			}
			
			int code = buffer.get();
			
			//Error and disconnect handling
			switch(code) {
				case 1:
					throw new ConnectionException(ConnectionStatus.DISCONNECTED);
				case 2:
					throw new ConnectionException(ConnectionStatus.FAILED_TO_KEEP_UP);
				case -1:
					int stringLength = buffer.getShort();
					throw new ConnectionException(ConnectionStatus.ERROR(new String(buffer.array(), buffer.position() + buffer.arrayOffset(), stringLength, StandardCharsets.UTF_8)));
			}
			
			return buffer;
		}

		@Override
		public void putHeader(ByteBuffer data) {
			data.putInt(ID);
		}
	}
	
	class TCPConnection implements Connection {
		COBSChannel channel;
		
		TCPConnection(InetAddress address, int port) throws IOException {
			channel = new COBSChannel(SocketChannel.open(new InetSocketAddress(address, port)));
		}
		
		@Override
		public void sendData(ByteBuffer data) throws ConnectionException {
			try {
				channel.write(data);
			} catch (IOException e) {
				throw new ConnectionException(ConnectionStatus.DROPPED, e);
			}
		}

		@Override
		public ByteBuffer waitForData() throws ConnectionException {
			ByteBuffer buffer = channel.getPacket();
			if(buffer == null)
				throw new ConnectionException(ConnectionStatus.DROPPED);

			int code = buffer.get();

			//Error and disconnect handling
			switch(code) {
			case 1:
				throw new ConnectionException(ConnectionStatus.DISCONNECTED);
			case 2:
				throw new ConnectionException(ConnectionStatus.FAILED_TO_KEEP_UP);
			case -1:
				int stringLength = buffer.getShort();
				throw new ConnectionException(ConnectionStatus.ERROR(new String(buffer.array(), buffer.position() + buffer.arrayOffset(), stringLength, StandardCharsets.UTF_8)));
			}

			return buffer;
		}

		@Override
		public void putHeader(ByteBuffer putInt) {}
	}
}
