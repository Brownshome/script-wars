package brownshome.scriptwars.client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The main class that clients can use to comunicate with the server.
 *  
 * Example Usage:
 * <pre>
 * <code>
 * public static main(String[] args) { 
 *  	Network.connect(123456);
 *  
 *  	while(Network.nextTick()) {
 *  		byte[][] grid = new byte[32][32];
 *  
 *  		int x = getByte();
 *  		int y = getByte();
 *  
 *  		for(int i = 0; i < 32; i++) {
 *  			for(int j = 0; j < 32; j++) {
 *  				grid[i][j] = getByte();
 *  			}
 *  		}
 *  
 *  		if(x < 30 && grid[x + 1][y] == 0) {
 *  			sendInt(1);
 *  		}
 *  	}
 *  }
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
	
	/** Call this using the ID given to you by the website to connect. */
	public static void connect(int ID, String ip, int port, String name) {
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
					connection = new UDPConnection(InetAddress.getByName(ip), port);
				} catch (SocketException | UnknownHostException e) {
					throw new RuntimeException("Unable to connect to " + ip + ":" + port, e);
				}
				break;
			default:
				throw new RuntimeException("Invalid ID");
		}
	}
	
	/** Waits until all the players have made their moves and sends the data.
	 * This method returns false if the game is over or you have timed out. */
	public static boolean nextTick() {
		dataOut.flip();
		connection.sendData(dataOut);
		dataOut.clear();
		dataOut.putInt(ID);
		dataIn = connection.waitForData();
		
		return dataIn != null;
	}
	
	/** Returns true if there is still data remaining. */
	public static boolean hasData() {
		return dataIn.hasRemaining();
	}
	
	/** Gets a single integer from the data. */
	public static int getInt() {
		return dataIn.getInt();
	}
	
	/** Gets a single byte from the data. */
	public static byte getByte() {
		return dataIn.get();
	}
	
	//Bit packing variables
	static int bit = 0x100;
	static int positionOfByte;
	
	/** Gets a true or false value from the data. */
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
	
	/** Gets a string from the data */
	public static String getString() {
		int length = dataIn.getShort();
		String result = new String(dataIn.array(), dataIn.arrayOffset() + dataIn.position(), length, StandardCharsets.UTF_8);
		dataIn.position(dataIn.position() + length);
		
		return result;
	}
	
	/** Gets the raw packet data, Only use this if you know what you are doing. */
	public static ByteBuffer getData() {
		return dataIn;
	}
	
	/** Gets a floating point number from the data */
	public static float getFloat() {
		return dataIn.getFloat();
	}
	
	public static void sendInt(int i) { dataOut.putInt(i); }
	public static void sendByte(byte i) { dataOut.put(i); }
	public static void sendFloat(float f) { dataOut.putFloat(f); }
	
	/** Sends raw data to the server. Only use this if you know what you are doing.
	 *
	 * @param data A byte array holding the data to send.
	 **/
	public static void sendData(byte[] data) { dataOut.put(data); }
	
	public static void sendString(String s) { 
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		dataOut.putShort((short) bytes.length);
		dataOut.put(bytes);
	}
}

//******************* INTERNAL USE ONLY BELOW THIS LINE ***********************************//

interface Connection {
	void sendData(ByteBuffer data);
	ByteBuffer waitForData();
}

class UDPConnection implements Connection {
	byte[] buffer = new byte[1024]; //If you need any larger than this use TCP
	DatagramSocket socket;
	InetAddress address;
	int port;
	
	UDPConnection(InetAddress address, int port) throws SocketException {
		this.address = address;
		this.port = port;
		socket = new DatagramSocket();
		socket.setSoTimeout(5000);
	}
	
	@Override
	public void sendData(ByteBuffer data) {
		try {
			socket.send(new DatagramPacket(data.array(), data.position(), data.remaining(), address, port));
		} catch (IOException e) {
			throw new RuntimeException("Error sending data", e);
		}
	}

	@Override
	public ByteBuffer waitForData() {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			throw new RuntimeException("Error receiving data", e);
		}
		
		ByteBuffer data = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
		int code = data.get();
		
		//Error and disconnect handling
		switch(code) {
			case 1:
				System.out.println("Disconnected by server.");
				return null;
			case 2:
				System.out.println("Failed to keep up with game tick.");
				return null;
			case -1:
				int stringLength = data.getShort();
				System.out.println("Server error: " + new String(data.array(), data.position() + data.arrayOffset(), stringLength, StandardCharsets.UTF_8));
				return null;
		}
		
		return data;
	}
}
