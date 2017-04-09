package brownshome.scriptwars.client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

import brownshome.scriptwars.server.connection.COBSChannel;

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
 *  	Network.connect(ID, "13.55.154.170", 35565, "John Smith");
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
	
	static Connection connection;
	static ByteBuffer dataOut = ByteBuffer.wrap(new byte[MAX_OUTPUT_SIZE]);
	static ByteBuffer dataIn;
	static int ID = -1;
	
	/**
	 * Call this using the ID given to you by the website to connect
	 * @param ID The ID given to you by the website
	 * @param ip The ip of the website
	 * @param name The name of your bot
	 */
	public static void connect(int ID, String ip, String name) {
		if(Network.ID != -1) {
			throw new IllegalStateException("Cannot initialize the connection more than once.");
		}
		
		Network.ID = ID;
		
		dataOut.clear();
		dataOut.putInt(ID);
		sendString(name);
		
		int protocol = ID >> 16 & 0xff;
		
		switch(protocol) {
			case 1:
				try {
					connection = new UDPConnection(InetAddress.getByName(ip), 35565);
				} catch (IOException e) {
					throw new RuntimeException("Unable to connect to " + ip, e);
				}
				break;
			case 2:
				try {
					connection = new TCPConnection(InetAddress.getByName(ip), 35566);
				} catch (UnknownHostException e) {
					throw new RuntimeException("Unable to connect to " + ip, e);
				}
				break;
			default:
				throw new RuntimeException("Invalid ID");
		}
	}
	
	/** Waits until all the players have made their moves and sends the data and retrieved a new set of data
	 * from the server. This method returns false if the game is over or you have timed out. 
	 * @return If the client was disconnected or timed out.
	 **/
	public static boolean nextTick() {
		dataOut.flip();
		connection.sendData(dataOut);
		dataOut.clear();
		connection.putHeader(dataOut);
		dataIn = connection.waitForData();
		
		return dataIn != null;
	}
	
	/**
	 * Checks if the next getX() will throw an exception. Note that even though
	 * this method returns false {@link #getBoolean()} may not error if there are booleans left
	 * in the buffer.
	 * @return true if there is at least one byte left to be read
	 */
	public static boolean hasData() {
		return dataIn.hasRemaining();
	}
	
	/**
	 * Gets a single integer from the data.
	 * @return An integer from the data
	 */
	public static int getInt() {
		return dataIn.getInt();
	}
	
	/**
	 * Gets a single byte from the data. This byte is returned as an integer in the
	 * range 0-255
	 * @return An integer containing the read byte.
	 */
	public static int getByte() {
		return dataIn.get();
	}
	
	//Bit packing variables
	static int bit = 0x100;
	static int positionOfByte;
	
	/** Gets a true or false value from the data. 
	 * @return A boolean read from the data.
	 **/
	public static boolean getBoolean() {
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
	public static String getString() {
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
	public static ByteBuffer getData() {
		return dataIn;
	}
	
	/** Gets a floating point number from the data 
	 * @return The decoded floating point value 
	 **/
	public static float getFloat() {
		return dataIn.getFloat();
	}
	
	/**
	 * Sends a single integer to the server.
	 * @param i The integer to send.
	 **/
	public static void sendInt(int i) { 
		dataOut.putInt(i); 
	}
	
	/**
	 * Sends a single byte to the server. This byte is send as a value from 0-255. If the
	 * integer contains values larger that that the lowest 8 bits will be taken.
	 * @param i An integer containing a value from 0-255.
	 */
	public static void sendByte(int i) { dataOut.put((byte) (i & 0xff)); }
	public static void sendFloat(float f) { dataOut.putFloat(f); }
	
	/** Sends raw data to the server. Only use this if you know what you are doing.
	 * Ensure that the limit and position are set properly.
	 * @param data A byte array holding the data to send.
	 **/
	public static void sendData(byte[] data) { 
		dataOut.put(data); 
	}
	
	/**
	 * Sends a String to the server.
	 * @param s The string to send to the server.
	 */
	public static void sendString(String s) { 
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		dataOut.putShort((short) bytes.length);
		dataOut.put(bytes);
	}
}

//******************* INTERNAL USE ONLY BELOW THIS LINE ***********************************//

interface Connection {
	void sendData(ByteBuffer data);
	void putHeader(ByteBuffer putInt);
	ByteBuffer waitForData();
}

class UDPConnection implements Connection {
	ByteBuffer buffer = ByteBuffer.allocate(1024); //If you need any larger than this use TCP
	DatagramChannel channel;
	
	UDPConnection(InetAddress address, int port) throws IOException {
		channel = DatagramChannel.open();
		channel.connect(new InetSocketAddress(address, port));
		channel.socket().setSoTimeout(5000);
	}
	
	@Override
	public void sendData(ByteBuffer data) {
		try {
			channel.write(data);
		} catch (IOException e) {
			throw new RuntimeException("Error sending data", e);
		}
	}

	@Override
	public ByteBuffer waitForData() {
		try {
			buffer.clear();
			channel.read(buffer);
			buffer.flip();
		} catch (IOException e) {
			throw new RuntimeException("Error receiving data", e);
		}
		
		int code = buffer.get();
		
		//Error and disconnect handling
		switch(code) {
			case 1:
				System.out.println("Disconnected by server.");
				return null;
			case 2:
				System.out.println("Failed to keep up with game tick.");
				return null;
			case -1:
				int stringLength = buffer.getShort();
				System.out.println("Server error: " + new String(buffer.array(), buffer.position() + buffer.arrayOffset(), stringLength, StandardCharsets.UTF_8));
				return null;
		}
		
		return buffer;
	}

	@Override
	public void putHeader(ByteBuffer data) {
		data.putInt(Network.ID);
	}
}

class TCPConnection implements Connection {
	COBSChannel channel;
	
	TCPConnection(InetAddress address, int port) {
		try {
			channel = new COBSChannel(SocketChannel.open(new InetSocketAddress(address, port)));
		} catch (IOException e) {
			throw new RuntimeException("Unable to form TCP connection", e);
		}
	}
	
	@Override
	public void sendData(ByteBuffer data) {
		try {
			channel.write(data);
		} catch (IOException e) {
			throw new RuntimeException("Unable to send data", e);
		}
	}

	@Override
	public ByteBuffer waitForData() {
		ByteBuffer buffer = channel.getPacket();
		if(buffer == null)
			throw new RuntimeException("Connection Dropped");

		int code = buffer.get();

		//Error and disconnect handling
		switch(code) {
		case 1:
			System.out.println("Disconnected by server.");
			return null;
		case 2:
			System.out.println("Failed to keep up with game tick.");
			return null;
		case -1:
			int stringLength = buffer.getShort();
			System.out.println("Server error: " + new String(buffer.array(), buffer.position() + buffer.arrayOffset(), stringLength, StandardCharsets.UTF_8));
			return null;
		}

		return buffer;
	}

	@Override
	public void putHeader(ByteBuffer putInt) {}
}
