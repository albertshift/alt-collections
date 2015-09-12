package alt.collections.paging;

/**
 * This class represents a logic to store reference to page in the store.
 * Can be implemented in different way.
 * 
 * Current implementation uses unsigned int to store page ref.
 * 
 * @author Albert Shift
 *
 */

public interface PageNum {

	public static PageNum DEFAULT = new UnsignedIntPageNum();
	
	long readPageNum(long address); 
	
	void writePageNum(long address, long pageNum);
	
	boolean casPageNum(long address, long oldPageNum, long newPageNum);
	
	int size();

}
