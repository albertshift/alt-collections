package alt.collections.store.impl;

import alt.collections.paging.InnerRef;
import alt.collections.paging.PageNum;
import alt.collections.store.MemStore;
import alt.collections.util.PageSize;
import alt.collections.util.Requires;
import alt.collections.util.Unsafe;

/**
 * Default MemStore.
 * 
 * @author Albert Shift
 *
 */
public class DefaultMemStore extends AbstractPagingStore implements MemStore {

	private final long totalPages;
	private long address;
	
	public DefaultMemStore(PageSize pageSize, PageNum pageNum, InnerRef innerRef, long size) {
		super(pageSize, pageNum, innerRef);
		this.totalPages = size / pageSize.getPageSize();
		this.address = Unsafe.INSTANCE.allocateMemory(size);
	}

	@Override
	public synchronized void free() {
		if (address != 0) {
			Unsafe.INSTANCE.freeMemory(address);
			address = 0;
		}
	}

	@Override
	public long getAddress(long pageNum) {
		Requires.positive(pageNum, "pageNum");
		Requires.less(pageNum, this.totalPages, "pageNum");

		return address + pageNum * pageSize.getPageSize();
	}

	@Override
	public long getTotalPages() {
		return this.totalPages;
	}

}
