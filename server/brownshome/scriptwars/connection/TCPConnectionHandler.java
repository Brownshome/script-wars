package brownshome.scriptwars.connection;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.logging.Level;

import brownshome.scriptwars.connection.COBSChannel;
import brownshome.scriptwars.game.*;
import brownshome.scriptwars.server.Server;

/**
 * The first packet sent contains the player ID and the player name
 */
public class TCPConnectionHandler extends ConnectionHandler<COBSChannel> {
	private static TCPConnectionHandler instance;
	
	public static TCPConnectionHandler instance() {
		if(instance == null) {
			instance = new TCPConnectionHandler();
		}
		
		return instance;
	}
	
	private final int PORT = 35566;
	private final int BUFFER_SIZE = 4096;
	private final int TCP_PROTOCOL_BYTE = 2;
	
	private Selector incomming;
	
	private TCPConnectionHandler() {
		try {
			incomming = Selector.open();
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress(PORT));
			serverSocket.configureBlocking(false);
			serverSocket.register(incomming, SelectionKey.OP_ACCEPT);

			Thread connectionAcceptThread = new Thread(this::listenLoop, "TCP Connection Accept Thread");
			connectionAcceptThread.setDaemon(true);
			connectionAcceptThread.start();
		} catch(IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to create TCP listener", e);
		}
	}
	
	private void listenLoop() {
		while(true) {
			try {
				incomming.select();
				for(Iterator<SelectionKey> iterator = incomming.selectedKeys().iterator(); iterator.hasNext(); ) {
					SelectionKey key = iterator.next();
					iterator.remove();
					
					if(!key.isValid())
						continue;
					
					try {
						if(key.isAcceptable()) {
							acceptConnection(key);
						}

						if(key.isReadable()) {
							if(key.attachment() instanceof Player) {
								@SuppressWarnings("unchecked")
								Player<COBSChannel> player = (Player<COBSChannel>) key.attachment();
								readData(player);
							} else {
								readData(key);
							}
						}
						
						if(key.isWritable()) {
							writeToConnection(key);
						}
					} catch(CancelledKeyException kce) {
						if(key.channel().isOpen())
							key.channel().close();
					} catch(IOException e) {
						Server.LOG.log(Level.WARNING, "TCP Error", e);
						key.cancel();
						if(key.channel().isOpen())
							key.channel().close();
					}
				}
			} catch(ClosedSelectorException cse) {
				Server.LOG.info("TCP Listener shutting down");
				return;
			} catch (IOException e1) {
				Server.LOG.log(Level.WARNING, "Error in TCP Listener", e1);
			}
		}
	}
	
	private void writeToConnection(SelectionKey key) {
		@SuppressWarnings("unchecked")
		Player<COBSChannel> player = (Player<COBSChannel>) key.attachment();
		
		if(player.getConnection().write()) {
			finishWrite((SocketChannel) key.channel());
		}
	}

	/** Called when the first packet has not yet arrived, we don't know who the player is. */
	private void readData(SelectionKey key) throws IOException {
		COBSChannel channel = (COBSChannel) key.attachment();
		ByteBuffer packet = channel.getPacket();
		
		if(packet == null)
			return;
		
		int ID = packet.getInt();

		Player<COBSChannel> player;
		
		try {
			player = new Player<>(ID, ConnectionUtil.bufferToString(packet), this, channel);
		} catch(ProtocolException pe) {
			//Error send by player class. Just return;
			return;
		}
		
		key.attach(player);
		
		try {
			synchronized(player.getGame()) {
				player.addPlayer();
			}
		} catch(IllegalArgumentException iae) {
			player.sendInvalidIDError();
		}
	}

	private void readData(Player<COBSChannel> player) throws IOException {
		ByteBuffer packet = player.getConnection().getPacket();

		if(packet != null) {
			try {
				synchronized(player.getGame()) {
					player.incommingData(packet);
				}
			} catch(Exception e) {
				player.sendError("Error processing packet " + e.getMessage());
			}
		} else {
			if(player.getConnection().isClosed()) {
				player.silentTimeOut();
			}
		}
	}

	/** Called when a connection has been accepted */
	private void acceptConnection(SelectionKey key) throws IOException {
		ServerSocketChannel channel = (ServerSocketChannel) key.channel();
		SocketChannel clientChannel = channel.accept();
		clientChannel.configureBlocking(false);
		clientChannel.register(incomming, SelectionKey.OP_READ).attach(new COBSChannel(clientChannel));
	}

	@Override
	public int getProtocolByte() {
		return TCP_PROTOCOL_BYTE;
	}

	@Override
	protected void closeConnection(COBSChannel connection) {
		try {
			connection.close();
		} catch (IOException e) {
			Server.LOG.log(Level.WARNING, "Unable to close the connection", e);
		}
	}
	
	@Override
	public void closeConnectionHandler() {
		try {
			incomming.close();
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to close the TCP Listener", e);
		}
	}

	@Override
	protected void sendRawData(COBSChannel channel, ByteBuffer... data) {
		try {
			writeListen(channel.channel);
			
			int length = 0;
			for(ByteBuffer b : data) {
				length += b.remaining();
			}
			ByteBuffer total = ByteBuffer.allocate(length);
			for(ByteBuffer b : data) {
				total.put(b);
			}
			total.flip();
			
			channel.write(total);
		} catch(IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	private void writeListen(SocketChannel channel) {
		SelectionKey key = channel.keyFor(incomming);
		key.interestOps(SelectionKey.OP_WRITE);
	}
	
	private void finishWrite(SocketChannel channel) {
		SelectionKey key = channel.keyFor(incomming);
		key.interestOps(SelectionKey.OP_READ);
	}
}
