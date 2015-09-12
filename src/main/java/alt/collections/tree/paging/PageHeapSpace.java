package alt.collections.tree.paging;

import alt.collections.concurrent.PageNumCas;


/**
 * Base interface for pages that organizes virtual space
 * 
 * @author Albert Shift
 *
 */

public interface PageHeapSpace {

	/**
	 * Very fast operation, returns -1 for no space in page
	 * 
	 * @param size
	 * @return
	 */
	
	int tryAllocate(int size);

	long getAddress();

	PageNumCas getNextPageNum();
	
	PageHeapSpace structNextPage(long pageNum);
	
	PageHeapSpace getNextPage(long pageNum);

}
