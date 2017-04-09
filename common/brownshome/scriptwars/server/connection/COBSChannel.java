package brownshome.scriptwars.server.connection;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/** This class performs COBS encoding on the underlying channel.
 * COBS(110) = 31110
 * COBS(11) = 3110
 * COBS() = 0 
 *
 * The byte indicates a run of n-1 bytes followed by a zero. The final zero is removed.
 **/
public class COBSChannel {
	private final SocketChannel channel;
	private ByteBuffer outputBuffer = ByteBuffer.allocate(64);
	private final ByteBuffer inputBuffer = ByteBuffer.allocate(1);
	private int nextReadHeader = -1;
	
	public COBSChannel(SocketChannel channel) {
		this.channel = channel;
		outputBuffer.flip();
		inputBuffer.flip();
	}
	
	public int write(ByteBuffer src) throws IOException {
		//COBS adds at most one byte every 254 bytes
		int written = src.remaining();
		ByteBuffer output = ByteBuffer.allocate(src.remaining() + src.remaining() / 254 + 2);
		
		byte lengthOfSection = 0x01;
		byte[] tmpBuffer = new byte[255];
		
		while(src.hasRemaining()) {
			byte in = src.get();
			if(in == 0) {
				output.put(lengthOfSection);
				output.put(tmpBuffer, 0, lengthOfSection - 1);
				lengthOfSection = 0x01;
			} else {
				tmpBuffer[lengthOfSection++ - 1] = in;
				
				if(lengthOfSection == (byte) 0xff) {
					output.put(lengthOfSection);
					output.put(tmpBuffer, 0, lengthOfSection - 1);
					lengthOfSection = 0x01;
				}
			}
		}
		
		output.put(lengthOfSection);
		output.put(tmpBuffer, 0, lengthOfSection - 1);
		
		output.put((byte) 0x00);
		output.flip();
		
		while(output.hasRemaining())
			channel.write(output);
		
		return written;
	}
	
	public ByteBuffer getPacket() {
		try {
			if(isClosed())
				return null;

			while(true) {
				if(nextReadHeader == -1) {
					int raw = read();

					if(raw == 0) {
						if(outputBuffer.position() == 0)
							return null;
						
						outputBuffer.limit(outputBuffer.limit() - 1);
						outputBuffer.flip();
						ByteBuffer result = outputBuffer;
						outputBuffer = ByteBuffer.allocate(64);
						outputBuffer.flip();
						return result;
					}

					if(raw == -1)
						return null;

					nextReadHeader = raw;
					setLimit(outputBuffer.position() + raw - 1);
				}

				while(outputBuffer.hasRemaining()) {
					if(channel.read(outputBuffer) < 1)
						return null;
				}

				setLimit(outputBuffer.position() + 1);

				if(nextReadHeader != 0xff)
					outputBuffer.put((byte) 0);

				nextReadHeader = -1;
			}
		} catch(IOException iex) {
			return null;
		}
	}

	private void setLimit(int l) {
		if(l > outputBuffer.capacity()) {
			ByteBuffer newBuffer = ByteBuffer.allocate(outputBuffer.capacity() * 2);
			outputBuffer.flip();
			newBuffer.put(outputBuffer);
			outputBuffer = newBuffer;
			
			setLimit(l);
		} else {
			outputBuffer.limit(l);
		}
	}

	private int read() throws IOException {
		if(inputBuffer.hasRemaining()) {
			return Byte.toUnsignedInt(inputBuffer.get());
		} else {
			inputBuffer.clear();
			int read = channel.read(inputBuffer);
			if(read < 1)
				return -1;
			
			inputBuffer.flip();
			return read();
		}
	}

	public boolean isClosed() {
		return !channel.isConnected();
	}

	public void close() {
		try {
			channel.socket().close();
		} catch (IOException e) {}
	}
}
