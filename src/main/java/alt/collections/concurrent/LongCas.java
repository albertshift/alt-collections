package alt.collections.concurrent;

import alt.collections.util.BigEndian;
import alt.collections.util.Unsafe;

/**
 * Atomic Operations under the Long
 * 
 * @author Albert Shift
 *
 */

public class LongCas {

	private final long address;
	private final int ref;
	
	public LongCas(long address, int ref) {
		this.address = address;
		this.ref = ref;
	}

	public long getLong() {
		long value = Unsafe.INSTANCE.getLong(address + ref);
		return BigEndian.ioLong(value);
	}
	
	public void putLong(long value) {
		Unsafe.INSTANCE.putLong(address + ref, BigEndian.ioLong(value));
	}
	
	public boolean casLong(long oldValue, long newValue) {
		return Unsafe.INSTANCE.compareAndSwapLong(null, address + ref,  BigEndian.ioLong(oldValue), BigEndian.ioLong(newValue));
	}
	
}
