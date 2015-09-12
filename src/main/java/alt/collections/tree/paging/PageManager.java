package alt.collections.tree.paging;

/**
 * Base interface to allocate and free pages in the store
 * 
 * @author Albert Shift
 *
 */

public interface PageManager {

	long allocatePage();
	
	void freePage(long pageNum, boolean delay);
	
}
