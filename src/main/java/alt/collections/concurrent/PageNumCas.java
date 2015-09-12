package alt.collections.concurrent;

import alt.collections.paging.PageNum;

/**
 * Atomic Operations under the PageNum
 * 
 * @author Albert Shift
 *
 */

public class PageNumCas {

	private final long address;
	private final int ref;
	private final PageNum pageNum;

	public PageNumCas(long address, int ref, PageNum pageNum) {
		this.address = address;
		this.ref = ref;
		this.pageNum = pageNum;
	}
	
	public long getPageNum() {
		return pageNum.readPageNum(address + ref);
	}

	public void putPageNum(long page) {
		this.pageNum.writePageNum(address + ref, page);
	}
	
	public boolean casPageNum(long oldPageNum, long newPageNum) {
		return pageNum.casPageNum(address + ref, oldPageNum, newPageNum);
	}
	
}

