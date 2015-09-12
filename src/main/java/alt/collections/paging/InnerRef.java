package alt.collections.paging;

/**
 * Inner Ref is used to store references inside a page
 * 
 * @author Albert Shift
 *
 */

public interface InnerRef {

	public static final InnerRef DEFAULT = new UnsignedShortInnerRef();
	
	int readInnerRef(long address); 
	
	void writeInnerRef(long address, int ref);
	
	boolean casInnerRef(long address, int oldRef, int newRef);
	
	int size();
	
}
