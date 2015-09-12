package alt.collections.paging;

import alt.collections.util.PageSize;

/**
 * Paging is used to address virtual memory by pages
 * 
 * page number is an unsigned int, that's why it is using long
 * 
 * max address space for 4096 page size is MAX_UNSIGNED_INT * 4096 = 4 billion * 4096 = 16 Tb
 * 
 * each PageNum is 32 bit unsigned int
 * 
 * @author Albert Shift
 *
 */

public interface Paging {

	PageSize pageSize();
	
	long getAddress(long pageNum);

	long getTotalPages();
	
	PageNum getPageNum();
	
	InnerRef getInnerRef();
	
	int getPageSize();
	
}
