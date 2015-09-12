package alt.collections.util;

/**
 * Requires parameters
 * 
 * @author Albert Shift
 *
 */
public final class Requires {

	public static void nonNull(Object obj, String argName) {
		if (obj == null) {
			throw new IllegalArgumentException("null argument '" + argName + "'");
		}
	}
	
	public static void nonZero(long value, String argName) {
		if (value == 0L) {
			throw new IllegalArgumentException("zero argument '" + argName + "'");
		}
	}
	
	public static void positive(long value, String argName) {
		if (value < 0) {
			throw new IllegalArgumentException("not a positive argument '" + argName + "' is " + value);
		}
	}
	
	public static void greater(long v, long than, String argName) {
		if (v <= than) {
			throw new IllegalStateException("argument "+ argName + " that is " + v + " must be greater than " + than);
		}
	}
	
	public static void greaterOrEquals(long v, long than, String argName) {
		if (v < than) {
			throw new IllegalStateException("argument "+ argName + " that is " + v + " must be greater or equals than " + than);
		}
	}
	
	public static void less(long v, long than, String argName) {
		if (v >= than) {
			throw new IllegalStateException("argument " + argName + " that is " + v + " must be less than " + than);
		}
	}
	
	public static void lessOrEquals(long v, long than, String argName) {
		if (v > than) {
			throw new IllegalStateException("argument " + argName + " that is " + v + " must be less or equals than " + than);
		}
	}
	
	public static void range(long v, long min, long max, String argName) {
    	if (v < min || v > max) {
    		throw new IndexOutOfBoundsException("argument " + argName + " is outside of the range [" + min + ".." + max + "]");
    	}
	}
	
	public static void aligned(PageSize pageSize, long value, String argName) {
    	if (!pageSize.isAligned(value)) {
    		throw new IllegalArgumentException("argument " + argName + " that is " + value + " must be aligned to " + pageSize.getPageSize());
    	}
	}
	
}
