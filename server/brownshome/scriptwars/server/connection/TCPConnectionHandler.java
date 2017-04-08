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
public class TCPConnectionHandler extends ConnectionHandler {
	private static final int PORT = 35566;
	private static ExecutorService TCP_POOL = Executors.newCachedThreadPool();
	private static final int BUFFER_SIZE = 4096;
	
	public static final int TCP_PROTOCOL_BYTE = 2;
	
	ByteChannel[] channels = new ByteChannel[256];
	
	public static void startTCPListener() {
		Thread connectionAcceptThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(PORT);) {
				while(!Server.shouldStop()) {
					acceptConnection(serverSocket);
				}
			} catch (IOException exception) {
				Server.LOG.log(Level.SEVERE, "Unable to create server socket", exception);
			}
		}, "TCP Connection Accept Thread");
		
		connectionAcceptThread.setDaemon(true);
		connectionAcceptThread.start();
	}
	
	private static void acceptConnection(ServerSocket serverSocket) {
		try (
			Socket clientSocket = serverSocket.accept();
			SocketChannel socketChannel = clientSocket.getChannel();
			COBSChannel channel = new COBSChannel(clientSocket.getChannel());
		) {
			socketChannel.configureBlocking(false);
			createClientThread(channel);
		} catch (IOException exception) {
			Server.LOG.log(Level.SEVERE, "Unable to connect to client", exception);
		}
	}

	private static void createClientThread(COBSChannel channel) {
		TCP_POOL.execute(() -> {
			try {
				ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
				channel.read(buffer);
				buffer.flip();
				int ID = buffer.getInt();
				Player player = getPlayerFromID(ID);
				TCPConnectionHandler handler = (TCPConnectionHandler) player.getConnectionHander();
				
				synchronized (handler.game) {
					if(player.isActive()) {
						try {
							sendErrorPacket(channel, "That ID is in use. Please use this one: " + handler.game.getType().getUserID());
						} catch(GameCreationException e)  {
							sendErrorPacket(channel, "That ID is in use. Unable to create a new game");
						}

						return;
					} 
					
					handler.channels[player.getSlot()] = channel;
					player.firstData(buffer);
				}

				while(channel.read(buffer) != -1) {
					buffer.flip();

					synchronized (handler.game) {
						player.incommingData(buffer);
					}
				}
			} catch (Exception e) { /*die silently*/ }
		});
	}

	protected TCPConnectionHandler(Game game) {
		super(game);
	}

	@Override
	public int getProtocolByte() {
		return TCP_PROTOCOL_BYTE;
	}

	@Override
	public void sendData(Player player, ByteBuffer buffer) {
		sendPacket(channels[player.getSlot()], buffer);
	}

	@Override
	public void timeOutPlayer(Player player) {
		sendTimeoutPacket(channels[player.getSlot()]);
		disconnect(player);
	}

	@Override
	public void endGame(Player player) {
		sendDisconnectPacket(channels[player.getSlot()]);
		disconnect(player);
	}

	@Override
	public void sendError(Player player, String message) {
		sendErrorPacket(channels[player.getSlot()], message);
		disconnect(player);
	}
	
	private void disconnect(Player player) {
		channels[player.getSlot()] = null;
	}

	private static void sendErrorPacket(ByteChannel channel, String message) {
		try {
			byte[] bytes = message.getBytes();
			short length = (short) bytes.length;
			
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length + Byte.BYTES + Short.BYTES);
			buffer.put((byte) -1);
			buffer.putShort(length);
			buffer.put(bytes);
			buffer.flip();
			
			channel.write(buffer);
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send error packet", e);
		}
	}

	private static void sendDisconnectPacket(ByteChannel details) {
		try {
			details.write(ByteBuffer.wrap(new byte[] {1}));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	private static void sendTimeoutPacket(ByteChannel details) {
		try {
			details.write(ByteBuffer.wrap(new byte[] {2}));
		} catch (IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}

	private static void sendPacket(ByteChannel details, ByteBuffer data) {
		try {
			byte[] array = new byte[data.remaining() + 1];
			array[0] = 0;
			data.get(array, 1, array.length - 1);
	
			details.write(data);
			data.rewind();
		} catch(IOException e) {
			Server.LOG.log(Level.SEVERE, "Unable to send packet", e);
		}
	}
}
