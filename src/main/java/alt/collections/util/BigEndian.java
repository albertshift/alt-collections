package alt.collections.util;

import java.nio.ByteOrder;

/**
 * Simple big endian swapper
 * 
 * @author Albert Shift
 *
 */

public final class BigEndian {

	public static final boolean IS_BIG_ENDIAN = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);;
	
	public static boolean isBigEndian() {
		return IS_BIG_ENDIAN;
	}
	
	public static char ioChar(char value) {
		return IS_BIG_ENDIAN ? swapChar(value) : value;
	}
	
	public static short ioShort(short value) {
		return IS_BIG_ENDIAN ? swapShort(value) : value;
	}
	
	public static int ioInt(int value) {
		return IS_BIG_ENDIAN ? swapInt(value) : value;
	}
	
	public static long ioLong(long value) {
		return IS_BIG_ENDIAN ? swapLong(value) : value;
	}
	
	public static float ioFloat(float value) {
		return IS_BIG_ENDIAN ? swapFloat(value) : value;
	}
	
	public static double ioDouble(double value) {
		return IS_BIG_ENDIAN ? swapDouble(value) : value;
	}
	
	public static char swapChar(char value) {
		return (char) ((((value >> 0) & 0xff) << 8) | 
				       (((value >> 8) & 0xff) << 0));

	}

	public static short swapShort(short value) {
		return (short) ((((value >> 0) & 0xff) << 8) | 
				        (((value >> 8) & 0xff) << 0));

	}

	public static int swapInt(int value) {
		return (((value >> 0) & 0xff) << 24) | 
			   (((value >> 8) & 0xff) << 16) | 
			   (((value >> 16) & 0xff) << 8) | 
			   (((value >> 24) & 0xff) << 0);

	}

	public static long swapLong(long value) {
		return (((value >> 0) & 0xff) << 56) | 
			   (((value >> 8) & 0xff) << 48) |
			   (((value >> 16) & 0xff) << 40) |
			   (((value >> 24) & 0xff) << 32) |
			   (((value >> 32) & 0xff) << 24) |
			   (((value >> 40) & 0xff) << 16) |
			   (((value >> 48) & 0xff) << 8) | 
			   (((value >> 56) & 0xff) << 0);
	}

	public static float swapFloat(float value) {
		int intValue = Float.floatToIntBits(value);
		intValue = swapInt(intValue);
		return Float.intBitsToFloat(intValue);
	}

	public static double swapDouble(double value) {
		long longValue = Double.doubleToLongBits(value);
		longValue = swapLong(longValue);
		return Double.longBitsToDouble(longValue);
	}
	
}
