package brownshome.scriptwars.server.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import brownshome.scriptwars.server.Server;
import brownshome.scriptwars.server.game.Game;
import brownshome.scriptwars.server.game.GameCreationException;
import brownshome.scriptwars.server.game.Player;

/**
 * Communicates over UDP with the client library. Each packet received is formatted as follows.
 * 
 * int: ID
 * var: data
 * 
 * If there is no data the packet is only seen as a keepalive or the start of a connection.
 * 
 * For outgoing packets the first byte is a purpose code, 0 is game data, 1 is disconnect, 2 is timedOut, -1 is server error.
 */
public class UDPConnectionHandler extends ConnectionHandler {
	public static final int port = 35565;
	static DatagramSocket socket;

	static byte[] errorPacket; //TODO allow custom error messages 

	static class ConnectionDetails {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((address == null) ? 0 : address.hashCode());
			result = prime * result + port;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ConnectionDetails))
				return false;
			ConnectionDetails other = (ConnectionDetails) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			if (port != other.port)
				return false;
			return true;
		}

		InetAddress address;
		int port;

		ConnectionDetails(DatagramPacket packet) {
			port = packet.getPort();
			address = packet.getAddress();
		}
	}

	ConnectionDetails[] details = new ConnectionDetails[256];

	static {
		byte[] string = "Error processing packet".getBytes(StandardCharsets.UTF_8);
		errorPacket = new byte[Byte.BYTES + Short.BYTES + string.length];
		errorPacket[0] = -1;
		errorPacket[1] = (byte) ((string.length >> 8) & 0xff);
		errorPacket[2] = (byte) (string.length & 0xff);

		startListenerThread();
	}

	public static void startListenerThread() {
		try {
			socket = new DatagramSocket(35565);
		} catch (SocketException e) {
			Server.LOG.log(Level.SEVERE, "Unable to bind to port.", e);
			System.exit(1);
		}

		Thread listenerThread = new Thread(UDPConnectionHandler::listenLoop, "LISTENER-THREAD");
		listenerThread.start();
	}

	static void listenLoop() {
		byte[] buffer = new byte[1024];
		ByteBuffer passingBuffer = ByteBuffer.wrap(buffer);
		DatagramPacket packet = new DatagramPacket(buffer, 1024);

		while(true) {
			boolean recieved = false;
			
			try {
				socket.receive(packet);
				recieved = true;
				
				passingBuffer.position(packet.getOffset());
				passingBuffer.limit(packet.getLength());

				int ID = passingBuffer.getInt();
				int playerCode = ID & 0xff;
				int gameCode = (ID >> 8) & 0xff;

				Game game = Game.getGame(gameCode);

				if(game == null) {
					sendErrorPacket(new ConnectionDetails(packet), "Invalid ID");
					continue;
				}

				UDPConnectionHandler connectionHandler;
				try {
					connectionHandler = (UDPConnectionHandler) game.getConnectionHandler();
				} catch(ClassCastException cce) {
					Server.LOG.log(Level.SEVERE, "Incorrect Protcol byte", cce);
					sendErrorPacket(new ConnectionDetails(packet), "Invalid ID");
					continue;
				}

				Player player = connectionHandler.getPlayer(playerCode);

				if(player == null) {
					sendErrorPacket(new ConnectionDetails(packet), "Invalid ID");
					continue;
				}

				if(!player.isActive()) {
					short length = passingBuffer.getShort();
					String name = new String(passingBuffer.array(), passingBuffer.arrayOffset() + passingBuffer.position(), length, StandardCharsets.UTF_8); //TODO move to utility function?
					player.setName(name);
					
					connectionHandler.details[player.getSlot()] = new ConnectionDetails(packet);
					connectionHandler.makePlayerActive(player);
				} else {
					ConnectionDetails newDetails = new ConnectionDetails(packet);
					
					if(!newDetails.equals(connectionHandler.details[player.getSlot()])) {
						try {
							sendErrorPacket(newDetails, "That ID is in use. Please use this one: " + connectionHandler.game.getType().getUserID());
						} catch(GameCreationException e)  {
							sendErrorPacket(newDetails, "That ID is in use. Unable to create a new game");
						}
					}
					
					connectionHandler.incommingData(passingBuffer, player);
				}
			} catch (Exception e) {
				Server.LOG.log(Level.SEVERE, "Error processing packet", e);
				
				if(recieved)
					sendErrorPacket(new ConnectionDetails(packet), "Error processing packet " + e.getMessage());
			}
		}
	}

	static void sendErrorPacket(ConnectionDetails details, String message) {
		try {
			byte[] bytes = message.getBytes();
			short length = (short) bytes.length;
			
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length + Byte.BYTES + Short.BYTES);
			buffer.put((byte) -1);
			buffer.putShort(length);
			buffer.put(bytes);
			buffer.flip();
			
			socket.send(new DatagramPacket(buffer.array(), buffer.remaining(), details.address, details.port));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send error packet", e);
		}
	}

	static void sendDisconnectPacket(ConnectionDetails details) {
		try {
			socket.send(new DatagramPacket(new byte[] {1}, 1, details.address, details.port));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	static void sendTimeoutPacket(ConnectionDetails details) {
		try {
			socket.send(new DatagramPacket(new byte[] {2}, 1, details.address, details.port));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	static void sendPacket(ConnectionDetails details, ByteBuffer data) {
		try {
			byte[] array = new byte[data.remaining() + 1];
			array[0] = 0;
			data.get(array, 1, array.length - 1);

			socket.send(new DatagramPacket(array, array.length, details.address, details.port));
			data.rewind();
		} catch(IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	@Override
	int getProtocolByte() {
		return 1;
	}

	@Override
	void timeOutPlayer(Player player) {
		sendTimeoutPacket(details[player.getSlot()]);
		disconnect(player);
	}

	@Override
	void endGame(Player player) {
		sendDisconnectPacket(details[player.getSlot()]);
		disconnect(player);
	}

	@Override
	void sendData(Player player, ByteBuffer buffer) {
		sendPacket(details[player.getSlot()], buffer);
	}

	@Override
	void sendError(Player player, String message) {
		sendErrorPacket(details[player.getSlot()], message);
		disconnect(player);
	}
	
	private void disconnect(Player player) {
		details[player.getSlot()] = null;
	}
}
