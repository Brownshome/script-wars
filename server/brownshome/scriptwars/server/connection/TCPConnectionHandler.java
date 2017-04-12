package brownshome.scriptwars.server.connection;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.Level;

import brownshome.scriptwars.server.Server;
import brownshome.scriptwars.server.game.*;

/**
 * The first packet sent contains the player ID and the player name
 */
public class TCPConnectionHandler extends ConnectionHandler<COBSChannel> {
	private static final int PORT = 35566;
	private static ExecutorService TCP_POOL = Executors.newCachedThreadPool();
	private static final int BUFFER_SIZE = 4096;
	
	private static Selector incomming;
	
	public static final int TCP_PROTOCOL_BYTE = 2;
	
	public static void startTCPListener() {
		try {
			incomming = Selector.open();
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress(PORT));
			serverSocket.configureBlocking(false);
			serverSocket.register(incomming, SelectionKey.OP_ACCEPT);

			Thread connectionAcceptThread = new Thread(TCPConnectionHandler::listenLoop, "TCP Connection Accept Thread");
			connectionAcceptThread.setDaemon(true);
			connectionAcceptThread.start();
		} catch(IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to create TCP listener", e);
		}
	}
	
	private static void listenLoop() {
		class Data {
			COBSChannel channel;
			Player player = null;
			
			Data(SocketChannel channel) {
				this.channel = new COBSChannel(channel);
			}
		}
		
		while(!Server.shouldStop()) {
			try {
				incomming.select();
				for(SelectionKey key : incomming.selectedKeys()) {
					if(key.isAcceptable()) {
						ServerSocketChannel channel = (ServerSocketChannel) key.channel();
						SocketChannel clientChannel = channel.accept();
						if(clientChannel == null)
							continue;
						
						clientChannel.configureBlocking(false);
						clientChannel.register(incomming, SelectionKey.OP_READ, new Data(clientChannel));
					}

					if(key.isReadable()) {
						Data data = (Data) key.attachment();

						ByteBuffer packet = data.channel.getPacket();

						if(packet != null) {
							try {
								if(data.player == null) {
									data.player = createPlayer(packet);
									synchronized(data.player.getConnectionHander().game) {
										if(data.player.isActive()) {
											try {
												sendErrorPacket(data.channel, "That ID is in use. Please use this one: " + data.player.getConnectionHander().game.getType().getUserID());
											} catch(GameCreationException e)  {
												sendErrorPacket(data.channel, "That ID is in use. Unable to create a new game");
											}

											data.channel.close();
											continue;
										} else {
											data.player.firstData(packet);
											data.player.getConnectionHander().putMapping(data.channel, data.player);
										}
									}
								} else {
									synchronized(data.player.getConnectionHander().game) {
										data.player.incommingData(packet);
									}
								}
							} catch(Exception e) {
								if(key.attachment() != null) {
									Player player = ((Data) key.attachment()).player;
									if(player != null)
										player.sendError("Error processing packet " + e.getMessage());
								}
								
								((SocketChannel) key.channel()).socket().close();
							}
						} else {
							if(data.channel.isClosed()) {
								if(data.player != null)
									data.player.timeOut();
							}
						}

					}
				}
			} catch(IOException e) {
				Server.LOG.log(Level.WARNING, "TCP Error", e);
			}
		}
	}

	private static Player createPlayer(ByteBuffer packet) throws ProtocolException {
		int ID = packet.getInt();
		return getPlayerFromID(ID);
	}

	protected TCPConnectionHandler(Game game) {
		super(game);
	}

	@Override
	public int getProtocolByte() {
		return TCP_PROTOCOL_BYTE;
	}

	protected void disonnect(Player player) {
		getMapping(player).close();
		super.disconnect(player);
	}
	
	private static void sendErrorPacket(COBSChannel channel, String message) {
		sendPacket(channel, ByteBuffer.wrap(new byte[] {-1}), stringToBuffer(message));
	}

	private static void sendPacket(COBSChannel channel, ByteBuffer... data) {
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
			
			channel.write(total);
		} catch(IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	@Override
	protected void sendRawData(COBSChannel channel, ByteBuffer... data) {
		sendPacket(channel, data);
	}
}
