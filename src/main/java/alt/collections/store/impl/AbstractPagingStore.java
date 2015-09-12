package alt.collections.store.impl;

import alt.collections.paging.InnerRef;
import alt.collections.paging.PageNum;
import alt.collections.paging.Paging;
import alt.collections.util.PageSize;

/**
 * Abstract Paging Store.
 * 
 * Common part for memstore and filestore.
 * 
 * @author Albert Shift
 *
 */

public abstract class AbstractPagingStore implements Paging {

	protected final PageSize pageSize;
	protected final PageNum pageNum;
	protected final InnerRef innerRef;
	
	public AbstractPagingStore(PageSize pageSize, PageNum pageNum, InnerRef innerRef) {
		this.pageSize = pageSize;
		this.pageNum = pageNum;
		this.innerRef = innerRef;
	}
	
	@Override
	public PageSize pageSize() {
		return pageSize;
	}
	
	@Override
	public PageNum getPageNum() {
		return pageNum;
	}

	@Override
	public InnerRef getInnerRef() {
		return innerRef;
	}

	@Override
	public int getPageSize() {
		return pageSize.getPageSize();
	}
}
