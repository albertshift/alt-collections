package alt.collections.util;

public class FormatUtil {

	public static String getHexAddress(Object obj) {
		int address = System.identityHashCode(obj);
		long unsignedInt = UnsignedInt.toLong(address);
		return Long.toHexString(unsignedInt);
	}
	
}
