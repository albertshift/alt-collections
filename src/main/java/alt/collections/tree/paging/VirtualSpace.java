package alt.collections.tree.paging;

import alt.collections.paging.PageStream;

/**
 * Base interface that organize virtual space based on pages
 * 
 * @author Albert Shift
 *
 */

public interface VirtualSpace {

	int allocate(int size);

	void seek(PageStream stream, int virtualPos);
	
}
