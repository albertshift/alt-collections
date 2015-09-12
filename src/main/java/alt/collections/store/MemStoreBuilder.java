package alt.collections.store;

import alt.collections.store.impl.DefaultMemStore;
import alt.collections.util.PageSize;
import alt.collections.util.Requires;

/**
 * MemStore Builder
 * 
 * @author Albert Shift
 *
 */

public class MemStoreBuilder extends AbstractStoreBuilder<MemStoreBuilder> {

	private long size;
	
	public MemStoreBuilder() {
	}
	
	public MemStoreBuilder(long size) {
		this.size = size;
	}
	
	public MemStoreBuilder withSize(long size) {
		Requires.positive(size, "size");
		Requires.aligned(PageSize.UNSAFE_PAGESIZE, size, "pageSize");
		this.size = size;
		return this;
	}
	
	public MemStore build() throws Exception {
		Requires.positive(size, "size");
		return new DefaultMemStore(pageSize, pageNum, innerRef, size);
	}
}
