package brownshome.scriptwars.connection;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

import brownshome.scriptwars.connection.InvalidIDException;
import brownshome.scriptwars.game.*;
import brownshome.scriptwars.server.Server;

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
	private static UDPConnectionHandler instance;
	
	public static UDPConnectionHandler instance() {
		if(instance == null) {
			instance = new UDPConnectionHandler();
		}
		
		return instance;
	}
	
	public static final int PORT = 35565;
	public static final int UPD_PROTOCOL_BYTE = 1;
	
	private DatagramChannel channel;

	private UDPConnectionHandler() {
		try {
			channel = DatagramChannel.open();
			channel.socket().bind(new InetSocketAddress(PORT));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to connect to port", e);
		}

		Thread listenerThread = new Thread(this::listenLoop, "LISTENER-THREAD");
		listenerThread.start();
	}

	@Override
	public void closeConnectionHandler() {
		try {
			channel.close();
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to close the UDP Listener", e);
		}
	}
	
	private void listenLoop() {
		byte[] buffer = new byte[1024];
		ByteBuffer passingBuffer = ByteBuffer.wrap(buffer);
		SocketAddress address = null;
		
		while(true) {
			boolean recieved = false;
			
			try {
				passingBuffer.clear();
				address = channel.receive(passingBuffer);
				passingBuffer.flip();
				recieved = true;

				int ID = passingBuffer.getInt();
				Player<SocketAddress> player = Player.getPlayerFromID(ID);
				
				if(player == null) {
					try {
						player = new Player<>(ID, ConnectionUtil.bufferToString(passingBuffer), this, address);
					} catch(InvalidIDException pe) {
						//Error send by player class. Just return;
						continue;
					}
					
					try {
						synchronized(player.getGame()) {
							player.addPlayer();
						}
					} catch(InvalidIDException iae) {
						player.sendInvalidIDError();
					}
				} else {
						if(!address.equals(player.getConnection())) {
							sendError(address, ConnectionHandler.getInvalidIDError(player.getGame()));
						} else {
							synchronized(player.getGame()) {
								player.incommingData(passingBuffer);
							}
						}
				}
				
				
			} catch(ClosedChannelException ace) {
				return; //server shutting down
			}catch (Exception e) {
				Server.LOG.log(Level.SEVERE, "Error processing packet", e);
				
				if(recieved)
					sendError(address, "Error processing packet " + e.getMessage());
			}
		}
	}
	
	@Override
	public int getProtocolByte() {
		return UPD_PROTOCOL_BYTE;
	}

	@Override
	public void sendRawData(SocketAddress address, ByteBuffer... data) {
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
