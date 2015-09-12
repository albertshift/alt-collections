package alt.collections.paging;

import alt.collections.util.BigEndian;
import alt.collections.util.Unsafe;

/**
 * Integer Inner Ref is used for storing page references inside the page
 * 
 * @author Albert Shift
 *
 */

public class IntegerInnerRef implements InnerRef {

	@Override
	public int readInnerRef(long address) {
		int value = Unsafe.INSTANCE.getInt(address);
		return BigEndian.ioInt(value);
	}

	@Override
	public void writeInnerRef(long address, int ref) {
		Unsafe.INSTANCE.putInt(address, BigEndian.ioInt(ref));
	}

	@Override
	public boolean casInnerRef(long address, int oldRef, int newRef) {
		return Unsafe.INSTANCE.compareAndSwapInt(null, address,  BigEndian.ioInt(oldRef), BigEndian.ioInt(newRef));
	}

	@Override
	public int size() {
		return 4;
	}

}
