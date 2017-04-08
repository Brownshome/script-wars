package brownshome.scriptwars.server.connection;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/** This class performs COBS encoding on the underlying channel. */
public class COBSChannel {
	private final SocketChannel channel;
	private ByteBuffer outputBuffer = ByteBuffer.allocate(64);
	private final ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
	
	public COBSChannel(SocketChannel channel) {
		this.channel = channel;
		inputBuffer.flip();
		outputBuffer.flip();
	}
	
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

	/** Buffers may be reused 
	 * @throws IOException */
	public ByteBuffer getPacket() throws IOException {
		byte length;
		int start = outputBuffer.position();
		int limit = outputBuffer.limit();
		while(true) {
			int raw = read();
			
			if(raw == 0) {
				outputBuffer.flip();
				return outputBuffer;
			}
			
			if(raw == -1)
				return null;
			
			length = (byte) raw;
			
			setLimit(outputBuffer.position() + Byte.toUnsignedInt(length) - 1);
			
			while(outputBuffer.hasRemaining()) {
				if(channel.read(outputBuffer) < 1)
					return null;
			}
			
			setLimit(outputBuffer.position() + 1);
			
			if(length != (byte) 0xff)
				outputBuffer.put((byte) 0);
		}
	}

	private void setLimit(int l) {
		if(l > outputBuffer.capacity()) {
			ByteBuffer newBuffer = ByteBuffer.allocate(outputBuffer.capacity() * 2);
			outputBuffer.flip();
			newBuffer.put(outputBuffer);
			outputBuffer = newBuffer;
		}
		
		outputBuffer.limit(l);
	}

	private int read() throws IOException {
		if(inputBuffer.hasRemaining()) {
			return Byte.toUnsignedInt(inputBuffer.get());
		} else {
			int read = channel.read(inputBuffer);
			if(read < 1)
				return -1;
			
			inputBuffer.flip();
			return read();
		}
	}

	public boolean isClosed() {
		return !channel.isOpen();
	}
}
