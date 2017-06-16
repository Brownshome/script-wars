package brownshome.scriptwars.connection;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
}
