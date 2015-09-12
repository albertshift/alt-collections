package alt.collections.store;

import alt.collections.paging.InnerRef;
import alt.collections.paging.PageNum;
import alt.collections.util.PageSize;
import alt.collections.util.Requires;

public class AbstractStoreBuilder<T extends AbstractStoreBuilder<T>> {

	protected PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	protected PageNum pageNum = PageNum.DEFAULT;
	protected InnerRef innerRef = InnerRef.DEFAULT;

	@SuppressWarnings("unchecked")
	public T withPageSize(int pageSize) {
		Requires.aligned(PageSize.UNSAFE_PAGESIZE, pageSize, "pageSize");
		this.pageSize = new PageSize(pageSize);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withPageNum(PageNum pageNum) {
		Requires.nonNull(pageNum, "pageNum");
		this.pageNum = pageNum;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withInnerRef(InnerRef innerRef) {
		Requires.nonNull(innerRef, "innerRef");
		this.innerRef = innerRef;
		return (T) this;
	}

}