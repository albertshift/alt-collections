package alt.collections.store;

import alt.collections.paging.Paging;

/**
 * MemStore interface
 * 
 * @author Albert Shift
 *
 */

public interface MemStore extends Paging {

	void free();
	
}
