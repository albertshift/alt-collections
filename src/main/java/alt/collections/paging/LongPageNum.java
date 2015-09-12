package alt.collections.paging;

import alt.collections.util.BigEndian;
import alt.collections.util.Unsafe;

/**
 * This class uses Long to store PageNums in pages
 * 
 * @author Albert Shift
 *
 */

public class LongPageNum implements PageNum {

	@Override
	public long readPageNum(long address) {
		long value = Unsafe.INSTANCE.getLong(address);
		return BigEndian.ioLong(value);
	}

	@Override
	public void writePageNum(long address, long pageNum) {
		Unsafe.INSTANCE.putLong(address, BigEndian.ioLong(pageNum));
	}

	@Override
	public boolean casPageNum(long address, long oldPageNum, long newPageNum) {
		return Unsafe.INSTANCE.compareAndSwapLong(null, address,  BigEndian.ioLong(oldPageNum), BigEndian.ioLong(newPageNum));
	}

	@Override
	public int size() {
		return 8;
	}

}
