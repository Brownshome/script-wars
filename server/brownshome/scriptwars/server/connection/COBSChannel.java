package brownshome.scriptwars.server.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/** This class performs COBS encoding on the underlying channel. This is not
 * threadsafe. */
public class COBSChannel implements ByteChannel {
	private final ByteChannel channel;
	private final ByteBuffer singleByteBuffer = ByteBuffer.allocate(1);
	
	public COBSChannel(ByteChannel channel) {
		this.channel = channel;
	}
	
	/**
	 * This method will read a packet from the socket. If the
	 * packet is longer than the buffer an exception will be thrown
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		byte length;
		int start = dst.position();
		int limit = dst.limit();
		while(true) {
			int raw = read();
			
			if(raw == 0)
				return dst.position() - start;
			
			if(raw == -1)
				return -1;
			
			length = (byte) raw;
			
			dst.limit(dst.position() + Byte.toUnsignedInt(length) - 1);
			
			while(dst.hasRemaining()) {
				if(channel.read(dst) == -1)
					return -1;
			}
			
			dst.limit(limit);
			
			if(length != (byte) 0xff)
				dst.put((byte) 0);
		}
	}

	private int read() throws IOException {
		singleByteBuffer.clear();
		channel.read(singleByteBuffer);
		return singleByteBuffer.get();
	}
	
	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		//COBS adds at most one byte every 254 bytes
		int written = src.remaining();
		ByteBuffer output = ByteBuffer.allocate(src.remaining() + src.remaining() / 254 + 2);
		
		byte lengthOfSection = 0x01;
		while(src.hasRemaining()) {
			byte in = src.get();
			if(in == 0) {
				output.put(lengthOfSection);
				lengthOfSection = 0x01;
			} else {
				lengthOfSection++;
				output.put(in);
				
				if(lengthOfSection == (byte) 0xff) {
					output.put(lengthOfSection);
					lengthOfSection = 0x01;
				}
			}
		}
		
		output.put((byte) 0x00);
		output.flip();
		
		while(output.hasRemaining())
			channel.write(output);
		
		return written;
	}
}
