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
	public static final int PORT = 35565;
	public static final int UPD_PROTOCOL_BYTE = 1;
	
	private static class ConnectionDetails {
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
	
		private InetAddress address;
		private int port;
	
		private ConnectionDetails(DatagramPacket packet) {
			port = packet.getPort();
			address = packet.getAddress();
		}
	}

	private static DatagramSocket socket;

	private ConnectionDetails[] details = new ConnectionDetails[256];

	public static void startListenerThread() {
		try {
			socket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			Server.LOG.log(Level.SEVERE, "Unable to bind to port.", e);
			System.exit(1);
		}

		Thread listenerThread = new Thread(UDPConnectionHandler::listenLoop, "LISTENER-THREAD");
		listenerThread.start();
	}

	public static void stop() {
		socket.close();
	}
	
	private static void listenLoop() {
		byte[] buffer = new byte[1024];
		ByteBuffer passingBuffer = ByteBuffer.wrap(buffer);
		DatagramPacket packet = new DatagramPacket(buffer, 1024);

		while(!Server.shouldStop()) {
			boolean recieved = false;
			
			try {
				socket.receive(packet);
				recieved = true;
				
				passingBuffer.position(packet.getOffset());
				passingBuffer.limit(packet.getLength());

				int ID = passingBuffer.getInt();
				int protocol = (ID >> 16) & 0xff;
				int playerCode = ID & 0xff;
				int gameCode = (ID >> 8) & 0xff;

				if(protocol != UPD_PROTOCOL_BYTE) {
					sendErrorPacket(new ConnectionDetails(packet), "Incorrect Protocol Byte for UDP");
					continue;
				}
				
				Game game = Game.getGame(gameCode);

				if(game == null) {
					sendErrorPacket(new ConnectionDetails(packet), "Invalid ID");
					continue;
				}

				UDPConnectionHandler connectionHandler;
				try {
					connectionHandler = (UDPConnectionHandler) game.getConnectionHandler(UPD_PROTOCOL_BYTE);
				} catch(ClassCastException cce) {
					Server.LOG.log(Level.SEVERE, "Incorrect Protcol byte", cce);
					sendErrorPacket(new ConnectionDetails(packet), "Invalid ID");
					continue;
				}
				
				connectionHandler.handlePacket(playerCode, packet, passingBuffer);
			} catch (Exception e) {
				Server.LOG.log(Level.SEVERE, "Error processing packet", e);
				
				if(recieved)
					sendErrorPacket(new ConnectionDetails(packet), "Error processing packet " + e.getMessage());
			}
		}
	}

	protected UDPConnectionHandler(Game game) {
		super(game);
	}
	
	private void handlePacket(int playerCode, DatagramPacket packet, ByteBuffer passingBuffer) {
		//There are faster ways to do this, but this shouldn't be too bad.
		//The only issue is that slow games could cause fast games to be starved as they are denied access to the Network thread
		synchronized(game) {
			Player player = getPlayer(playerCode);
			if(player == null) {
				sendErrorPacket(new ConnectionDetails(packet), "Invalid ID");
				return;
			}

			if(!player.isActive()) {
				short length = passingBuffer.getShort();
				String name = new String(passingBuffer.array(), passingBuffer.arrayOffset() + passingBuffer.position(), length, StandardCharsets.UTF_8); //TODO move to utility function?
				player.setName(name);

				details[player.getSlot()] = new ConnectionDetails(packet);
				game.makePlayerActive(player);
			} else {
				ConnectionDetails newDetails = new ConnectionDetails(packet);

				if(!newDetails.equals(details[player.getSlot()])) {
					try {
						sendErrorPacket(newDetails, "That ID is in use. Please use this one: " + game.getType().getUserID());
					} catch(GameCreationException e)  {
						sendErrorPacket(newDetails, "That ID is in use. Unable to create a new game");
					}
				}

				game.incommingData(passingBuffer, player);
			}
		}
	}

	@Override
	protected int getProtocolByte() {
		return UPD_PROTOCOL_BYTE;
	}

	@Override
	public void sendData(Player player, ByteBuffer buffer) {
		sendPacket(details[player.getSlot()], buffer);
	}

	@Override
	public void timeOutPlayer(Player player) {
		sendTimeoutPacket(details[player.getSlot()]);
		disconnect(player);
	}

	@Override
	public void endGame(Player player) {
		sendDisconnectPacket(details[player.getSlot()]);
		disconnect(player);
	}

	@Override
	public void sendError(Player player, String message) {
		sendErrorPacket(details[player.getSlot()], message);
		disconnect(player);
	}
	
	private void disconnect(Player player) {
		details[player.getSlot()] = null;
	}

	private static void sendErrorPacket(ConnectionDetails details, String message) {
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

	private static void sendDisconnectPacket(ConnectionDetails details) {
		try {
			socket.send(new DatagramPacket(new byte[] {1}, 1, details.address, details.port));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	private static void sendTimeoutPacket(ConnectionDetails details) {
		try {
			socket.send(new DatagramPacket(new byte[] {2}, 1, details.address, details.port));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	private static void sendPacket(ConnectionDetails details, ByteBuffer data) {
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
}
