package alt.collections.paging;

import alt.collections.util.BigEndian;
import alt.collections.util.Unsafe;
import alt.collections.util.UnsignedInt;

/**
 * This class uses Unsigned Int to store PageNums in Pages
 * 
 * @author Albert Shift
 *
 */

public class UnsignedIntPageNum implements PageNum {

	@Override
	public long readPageNum(long address) {
		int ivalue = Unsafe.INSTANCE.getInt(address);
		ivalue = BigEndian.ioInt(ivalue);
		return UnsignedInt.toLong(ivalue);
	}

	@Override
	public void writePageNum(long address, long pageNum) {
		int ivalue = UnsignedInt.fromLong(pageNum);
		ivalue = BigEndian.ioInt(ivalue);
		Unsafe.INSTANCE.putInt(address, ivalue);
	}

	@Override
	public boolean casPageNum(long address, long oldPageNum, long newPageNum) {
		int iold = UnsignedInt.fromLong(oldPageNum);
		iold = BigEndian.ioInt(iold);
		
		int inew = UnsignedInt.fromLong(newPageNum);
		inew = BigEndian.ioInt(inew);
		
		return Unsafe.INSTANCE.compareAndSwapInt(null, address, iold, inew);
	}

	@Override
	public int size() {
		return 4;
	}

}
