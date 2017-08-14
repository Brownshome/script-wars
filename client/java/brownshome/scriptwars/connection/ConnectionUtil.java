package brownshome.scriptwars.connection;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;

public class ConnectionUtil {
	public static String bufferToString(ByteBuffer buffer) {
		int length = buffer.getShort();
		return new String(buffer.array(), buffer.position() + buffer.arrayOffset(), length, StandardCharsets.UTF_8);
	}

	public static ByteBuffer stringToBuffer(String message) {
		byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
		ByteBuffer result = ByteBuffer.allocate(bytes.length + Short.BYTES);
		result.putShort((short) bytes.length);
		result.put(bytes);
		result.flip();
		
		return result;
	}
	
	
	/** A utility class for reading and writing booleans. This is designed as the opposite operation of the Network class.
	 * A single write uses a byte of data, and that byte is slowly filled up with bits. */
	public static class BooleanWriter {
		private byte buffer = 0;
		private int index = 0;
		private int position = -1;
		private final ByteBuffer data;
		
		public BooleanWriter(ByteBuffer data) {
			this.data = data;
		}
		
		public void writeBoolean(boolean bool) {
			if(position == -1) {
				position = data.position();
				data.get(); //Increment position
			}
			
			buffer |= (bool ? 1 : 0) << index++;
			
			if(index == 8) {
				index = 0;
				data.put(position, buffer);
				buffer = 0;
				position = -1;
			}
		}
		
		public void flush() {
			if(index != 0) {
				data.put(position, buffer);
			}
			
			index = 0;
			buffer = 0;
			position = -1;
		}
	}
}
