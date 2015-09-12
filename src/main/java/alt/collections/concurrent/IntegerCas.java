package alt.collections.concurrent;

import alt.collections.util.BigEndian;
import alt.collections.util.Unsafe;

/**
 * Atomic Operations under the Integer
 * 
 * @author Albert Shift
 *
 */

public class IntegerCas {

	private final long address;
	private final int ref;
	
	public IntegerCas(long address, int ref) {
		this.address = address;
		this.ref = ref;
	}

	public int getInt() {
		int value = Unsafe.INSTANCE.getInt(address + ref);
		return BigEndian.ioInt(value);
	}
	
	public void putInt(int value) {
		Unsafe.INSTANCE.putInt(address + ref, BigEndian.ioInt(value));
	}
	
	public boolean casInt(int oldValue, int newValue) {
		return Unsafe.INSTANCE.compareAndSwapInt(null, address + ref,  BigEndian.ioInt(oldValue), BigEndian.ioInt(newValue));
	}
	
}
