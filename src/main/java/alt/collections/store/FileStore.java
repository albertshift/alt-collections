package alt.collections.store;

import java.io.Closeable;

import alt.collections.paging.Paging;

/**
 * FileStore interface
 * 
 * @author Albert Shift
 *
 */

public interface FileStore extends Paging, Closeable {

	void close();
	
}
