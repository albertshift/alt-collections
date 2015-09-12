package alt.collections.store.impl;

import java.io.File;

import alt.collections.paging.InnerRef;
import alt.collections.paging.PageNum;
import alt.collections.store.FileStore;
import alt.collections.store.FileStoreBuilder.FileEntry;
import alt.collections.util.MapFileMode;
import alt.collections.util.PageSize;
import alt.collections.util.Requires;
import alt.collections.util.UnsafeMemoryMappedFile;

/**
 * 
 * Single File Store implementation
 * 
 * @author Albert Shift
 *
 */

public class SingleFileStore  extends AbstractPagingStore implements FileStore {

	private final UnsafeMemoryMappedFile mmf;
	private final long totalPages;
	
	public SingleFileStore(PageSize pageSize, PageNum pageNum, InnerRef innerRef, MapFileMode fileMode, FileEntry file, boolean deleteOnExit) throws Exception {
		super(pageSize, pageNum, innerRef);
		this.mmf = new UnsafeMemoryMappedFile(file.getFilePath(), fileMode, file.getSize());
		this.totalPages = mmf.getSize() / pageSize.getPageSize();
		
		if (deleteOnExit) {
			new File(file.getFilePath()).deleteOnExit();
		}
	}
	
	@Override
	public void close() {
		mmf.close();
	}

	@Override
	public long getAddress(long pageNum) {
		Requires.positive(pageNum, "pageNum");
		Requires.less(pageNum, this.totalPages, "pageNum");
		
		return mmf.getAddress() + pageNum * pageSize.getPageSize();
	}

	@Override
	public long getTotalPages() {
		return this.totalPages;
	}

}
