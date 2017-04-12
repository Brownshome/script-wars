package brownshome.scriptwars.server.connection;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
 */
public class UDPConnectionHandler extends ConnectionHandler<SocketAddress> {
	public static final int PORT = 35565;
	public static final int UPD_PROTOCOL_BYTE = 1;
	
	private static DatagramChannel channel;

	public static void startListenerThread() {
		try {
			channel = DatagramChannel.open();
			channel.socket().bind(new InetSocketAddress(PORT));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to connect to port", e);
		}

		Thread listenerThread = new Thread(UDPConnectionHandler::listenLoop, "LISTENER-THREAD");
		listenerThread.start();
	}

	public static void stop() throws IOException {
		channel.close();
	}
	
	private static void listenLoop() {
		byte[] buffer = new byte[1024];
		ByteBuffer passingBuffer = ByteBuffer.wrap(buffer);
		SocketAddress address = null;
		
		while(!Server.shouldStop()) {
			boolean recieved = false;
			
			try {
				passingBuffer.clear();
				address = channel.receive(passingBuffer);
				passingBuffer.flip();
				recieved = true;

				int ID = passingBuffer.getInt();
				Player player = getPlayerFromID(ID);
				
				UDPConnectionHandler handler = (UDPConnectionHandler) player.getConnectionHander();
				
				synchronized(handler.game) {
					if(player.isActive()) {
						if(!address.equals(handler.getMapping(player))) {
							try {
								sendErrorPacket(address, "That ID is in use. Please use this one: " + handler.game.getType().getUserID());
							} catch(GameCreationException e)  {
								sendErrorPacket(address, "That ID is in use. Unable to create a new game");
							}
						} else {
							player.incommingData(passingBuffer);
						}
					} else {
						player.firstData(passingBuffer);
						handler.putMapping(address, player);
					}
				}
			} catch(AsynchronousCloseException ace) {
				return; //server shutting down
			}catch (Exception e) {
				Server.LOG.log(Level.SEVERE, "Error processing packet", e);
				
				if(recieved)
					sendErrorPacket(address, "Error processing packet " + e.getMessage());
			}
		}
	}

	protected UDPConnectionHandler(Game game) {
		super(game);
	}
	
	@Override
	public int getProtocolByte() {
		return UPD_PROTOCOL_BYTE;
	}

	@Override
	public void sendRawData(SocketAddress id, ByteBuffer... buffer) {
		sendPacket(id, buffer);
	}

	private static void sendErrorPacket(SocketAddress address, String message) {
		sendPacket(address, ByteBuffer.wrap(new byte[] {-1}), stringToBuffer(message));
	}

	private static void sendPacket(SocketAddress address, ByteBuffer... data) {
		try {
			int length = 0;
			for(ByteBuffer b : data) {
				length += b.remaining();
			}
			ByteBuffer total = ByteBuffer.allocate(length);
			for(ByteBuffer b : data) {
				total.put(b);
			}
			total.flip();
			
			channel.send(total, address);
		} catch(IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}
}
