package brownshome.scriptwars.connection;

import java.io.*;
import java.lang.reflect.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;
import java.util.logging.Level;

import brownshome.scriptwars.client.*;
import brownshome.scriptwars.game.Player;
import brownshome.scriptwars.server.Server;

public class MemoryConnectionHandler extends ConnectionHandler<SynchronousQueue<ByteBuffer>> {
	private static MemoryConnectionHandler instance;
	
	public static void runAI(String name, int ID) {
		try {
			Class<?> clazz = Class.forName(name);
			Method main = clazz.getMethod("main", String[].class);
			
			Thread aiThread = new Thread(() -> {
				try {
					main.invoke(null, String.valueOf(ID));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
			}, name + "@" + ID + " AI thread");
			
			aiThread.setDaemon(true);
			aiThread.start();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			Server.LOG.log(Level.WARNING, "Unable to start " + name, e);
		}
	}
	
	public static MemoryConnectionHandler instance() {
		if(instance == null) {
			instance = new MemoryConnectionHandler();
		}
		
		return instance;
	}
	
	private final int MEMORY_PROTOCOL_BYTE = 3;
	
	@Override
	protected void sendRawData(SynchronousQueue<ByteBuffer> queue, ByteBuffer... data) {
		int length = 0;
		for(ByteBuffer b : data) {
			length += b.remaining();
		}
		ByteBuffer total = ByteBuffer.allocate(length);
		for(ByteBuffer b : data) {
			total.put(b);
		}
		total.flip();
		
		//If the packet is not ready to be received drop it.
		queue.offer(total);
	}

	@Override
	public int getProtocolByte() {
		return MEMORY_PROTOCOL_BYTE;
	}

	@Override
	public void closeConnectionHandler() {}

	public Consumer<ByteBuffer> join(ByteBuffer firstPacket, SynchronousQueue<ByteBuffer> input) throws InvalidIDException {
		int ID = firstPacket.getInt();
		
		if(Player.getPlayerFromID(ID) != null) {
			throw new InvalidIDException();
		}
		
		Player<SynchronousQueue<ByteBuffer>> player = new Player<>(ID, ConnectionUtil.bufferToString(firstPacket), this, input);
		
		synchronized(player.getGame()) {
			player.addPlayer();
		}
		
		return buffer -> handleData(buffer, player);
	}

	private void handleData(ByteBuffer buffer, Player<SynchronousQueue<ByteBuffer>> player) {
		try {
			synchronized(player.getGame()) {
				player.incommingData(buffer);
			}
		} catch(Exception e) {
			player.sendError(e.getMessage());
		}
	}
}
