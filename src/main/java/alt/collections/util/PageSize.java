package alt.collections.util;

/**
 * Util class to manage page size
 * 
 * @author Albert Shift
 *
 */

public final class PageSize {

	public static final PageSize UNSAFE_PAGESIZE = new PageSize(Unsafe.INSTANCE.pageSize());
	
    private final int pageSize;
    private final long pageMask;

    public PageSize(int pageSize) {
    	this.pageSize = pageSize;
    	this.pageMask = ~((long)pageSize - 1L);
    }
    
    public int getPageSize() {
    	return pageSize;
    }
    
    public boolean isAligned(long address) {
    	long aligned = address & pageMask;
    	if (address != aligned) {
    		return false;
    	}
    	return true;
    }
    
    public long alignTop(long address) {
    	return address & pageMask;
    }
    
    public long alignBottom(long address) {
    	long aligned = address & pageMask;
    	if (address != aligned) {
    		aligned += pageSize;
    	}
    	return aligned;
    }

	@Override
	public String toString() {
		return "PageSize [pageSize=" + pageSize + "]";
	}

}
