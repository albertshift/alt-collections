package alt.collections.tree.paging;

import alt.collections.concurrent.InnerRefCas;
import alt.collections.concurrent.PageNumCas;
import alt.collections.paging.Paging;
import alt.collections.util.ThreadUtil;

/**
 * Abstract Master Page
 * 
 * @author Albert Shift
 *
 */

public abstract class AbstractMasterPage implements PageHeapSpace {

	protected final Paging paging;
	protected long address;
	protected char magic;
	protected InnerRefCas pageTail;
	protected PageNumCas nextPageNum;
	protected int heapSpacePos;
	
	public AbstractMasterPage(Paging paging) {
		this.paging = paging;
	}
	
	/**
	 * Allocate space in the page.
	 * 
	 * @param size
	 * @return -1 of no space or Position if space allocated
	 */
	
	@Override
	public int tryAllocate(int size) {
		while(true) {
			int tail = pageTail.getInnerRef();
			if (tail + size > paging.getPageSize()) {
				return -1;
			}
			if (pageTail.casInnerRef(tail, tail + size)) {
				return tail;
			}
			ThreadUtil.loopSleep();
		}
	}
	
	@Override
	public PageNumCas getNextPageNum() {
		return nextPageNum;
	}

	@Override
	public PageHeapSpace structNextPage(long pageNum) {
		MasterContinuePage.structBlank(paging, pageNum);
		return new MasterContinuePage(paging, pageNum, true);
	}

	@Override
	public PageHeapSpace getNextPage(long pageNum) {
		return new MasterContinuePage(paging, pageNum, false);
	}
	
	@Override
	public long getAddress() {
		return address;
	}
	
}
