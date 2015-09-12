package alt.collections.util;

public class UnsignedInt {

	public static int fromLong(long value) {
		return (int) value;
	}

	public static long toLong(int unsignedInt) {
		if (unsignedInt == -1) {
			return 0xFFFFFFFFL;
		}
		if (unsignedInt < 0) {
			int value = -unsignedInt;
			int addon = Integer.MAX_VALUE - value + 2;
			return (long) Integer.MAX_VALUE + addon;
		}
		return (long) unsignedInt;
	}
	

}
