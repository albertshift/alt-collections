package alt.collections.paging;

import alt.collections.util.BigEndian;
import alt.collections.util.Unsafe;
import alt.collections.util.UnsignedInt;

/**
 * Unsigned Short Inner Ref is used for pages with size less or equals 65535
 * 
 * @author Albert Shift
 *
 */

public class UnsignedShortInnerRef implements InnerRef {

	@Override
	public int readInnerRef(long address) {
		char value = Unsafe.INSTANCE.getChar(address);
		return BigEndian.ioChar(value);
	}

	@Override
	public void writeInnerRef(long address, int ref) {
		Unsafe.INSTANCE.putChar(address, BigEndian.ioChar((char) ref));
	}

	@Override
	public boolean casInnerRef(long address, int oldRef, int newRef) {

		long loldRef = oldRef; 
		long lnewRef = newRef;
		
		long upperValue = Unsafe.INSTANCE.getChar(address+2);

		long oldV = (loldRef & 0xffff) | ((upperValue & 0xffff) << 16);
		long newV = (lnewRef & 0xffff) | ((upperValue & 0xffff) << 16);
		
		int oldU = UnsignedInt.fromLong(oldV);
		int newU = UnsignedInt.fromLong(newV);
		
		return Unsafe.INSTANCE.compareAndSwapInt(null, address,  BigEndian.ioInt(oldU), BigEndian.ioInt(newU));
	}

	@Override
	public int size() {
		return 2;
	}

}
