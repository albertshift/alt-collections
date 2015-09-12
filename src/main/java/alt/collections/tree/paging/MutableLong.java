package alt.collections.tree.paging;

/**
 * Primitive Class to differ Immutable Long (Long) from Mutable Long
 * 
 * @author Albert Shift
 *
 */

public final class MutableLong {

	private long value;
	
	public MutableLong(long value) {
		this.value = value; 
	}
	
	public long longValue() {
		return value;
	}
	
	@Override
    public int hashCode() {
		return (int)(value ^ (value >>> 32));
    }
    
	@Override
	public String toString() {
		return Long.toString(value);
	}

}
