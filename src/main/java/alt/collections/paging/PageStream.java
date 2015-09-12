package alt.collections.paging;

import alt.collections.util.PageSize;
import alt.collections.util.Requires;

/**
 * Common access patterns based on Page adressing
 * 
 * @author Albert Shift
 *
 */

public class PageStream {

	protected final Paging paging;
	protected long address;
	protected int position;
    
    public PageStream(Paging paging) {
    	Requires.nonNull(paging, "paging");
    	
    	this.paging = paging;
    	this.address = 0;
    	this.position = 0;
    }
    
    public PageStream(Paging paging, long pageNum) {
    	Requires.nonNull(paging, "paging");
    	Requires.positive(pageNum, "pageNum");
    	
    	this.paging = paging;
    	this.address = paging.getAddress(pageNum);
    	this.position = 0;
    }
    
    public PageStream(PageStream other) {
    	this.paging = other.paging;
    	this.address = other.address;
    	this.position = other.position;
    }
	
	public int getPosition() {
		return position;
	}

	public Paging getPaging() {
		return paging;
	}

	public int getPositionAndSkip(int bytes) {
		ensureCanGrow(bytes);
		
		int result = position;
		position += bytes;
		return result;
	}
	
	public int getPositionAndSkipInnerRef() {
		return getPositionAndSkip(paging.getInnerRef().size());
	}
	
	public int getPositionAndSkipPageNum() {
		return getPositionAndSkip(paging.getPageNum().size());
	}
	
	public long getAddress() {
		return address;
	}

	public PageSize pageSize() {
		return paging.pageSize();
	}

	public void switchAddress(long address) {
		Requires.positive(address, "address");
		
		this.address = address;
		this.position = 0;
	}
	
	public void switchAddress(long address, int position) {
		Requires.positive(address, "address");
		Requires.positive(position, "position");
		ensureNewPosition(position);
		
		this.address = address;
		this.position = position;
	}
	
    public void reset() {
    	this.position = 0;
    }
    
    public void skip(int bytes) {
		ensureCanGrow(bytes);
		position += bytes;
    }
    
    public void seek(int position) {
    	ensureNewPosition(position);
    	
    	this.position = position;
    }
    
    public int bytesAvailable() {
    	return paging.getPageSize() - this.position;
    }
    
    protected void ensureCanGrow(int addon) {
    	int newPosition = position + addon;
    	Requires.range(newPosition, 0, paging.getPageSize(), "newPosition");
    }
    
    protected void ensureNewPosition(int newPosition) {
    	Requires.range(newPosition, 0, paging.getPageSize(), "newPosition");
    }
}
