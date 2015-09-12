package alt.collections.store.impl;

import java.io.File;
import java.util.List;

import alt.collections.paging.InnerRef;
import alt.collections.paging.PageNum;
import alt.collections.store.FileStore;
import alt.collections.store.FileStoreBuilder.FileEntry;
import alt.collections.util.MapFileMode;
import alt.collections.util.PageSize;
import alt.collections.util.Requires;
import alt.collections.util.UnsafeMemoryMappedFile;

/**
 * Multiple File Store
 * 
 * @author Albert Shift
 *
 */

public class MultipleFileStore extends AbstractPagingStore implements FileStore {
	
	private final Segment[] segments;
	private final long totalPages;
	
	public MultipleFileStore(PageSize pageSize, PageNum pageNum0, InnerRef innerRef, MapFileMode fileMode, List<FileEntry> fileEntries, boolean deleteOnExit) throws Exception {
		super(pageSize, pageNum0, innerRef);
		this.segments = new Segment[fileEntries.size()];
		
		long pageNum = 0;
		
		for (int i = 0; i != fileEntries.size(); ++i) {
			FileEntry file = fileEntries.get(i);
			
			UnsafeMemoryMappedFile mmf = null;
			try {
				mmf = new UnsafeMemoryMappedFile(file.getFilePath(), fileMode, file.getSize());
			} catch (Exception e) {
				close();
				throw e;
			}
			
			long segmentPages = file.getSize() / pageSize.getPageSize();
			
			this.segments[i] = new Segment(mmf, pageNum, segmentPages);
			
			pageNum += segmentPages;
			
			if (deleteOnExit) {
				new File(file.getFilePath()).deleteOnExit();
			}
			
		}
		
		this.totalPages = pageNum;
		
	}
	
	@Override
	public void close() {
		for (int i = 0; i != segments.length; ++i) {
			Segment segment = segments[i];
			if (segment != null) {
				segment.close();
			}
		}
	}

	@Override
	public long getAddress(long pageNum) {
		Requires.positive(pageNum, "pageNum");
		Requires.less(pageNum, this.totalPages, "pageNum");
		
		int index = search(pageNum);
		if (index > 0) {
			Segment segment = segments[index];
			return segment.getAddress(pageNum - segment.startPageNum);
		}
		
		index = -(index + 1);
		
		Segment segment = segments[index-1];
		return segment.getAddress(pageNum - segment.startPageNum);
		
	}

	@Override
	public long getTotalPages() {
		return this.totalPages;
	}
	
    private int compare(long thisVal, long anotherVal) {
    	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }
	
	private int binarySearch(Segment[] a, int fromIndex, int toIndex,
			long key) {

		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low >>> 1) + (high >>> 1);
			mid += ((low & 1) + (high & 1)) >>> 1;
			
			//int mid = (low + high) >>> 1;
			Segment midVal = a[mid];
			int cmp = compare(midVal.startPageNum, key);

			if (cmp < 0) {
				low = mid + 1;
			}
			else if (cmp > 0) {
				high = mid - 1;
			}
			else {
				return mid; // key found
			}
		}
		return -(low + 1); // key not found.
	}
	
	private int search(long key) {
		if (key > 7) {
			return binarySearch(segments, 0, segments.length, key);
		}
		else {
		    for (int i = 0; i != segments.length; i++) {
		    	int c = compare(segments[i].startPageNum, key);
				if (c == 0) {
				    return i;
				}
				else if (c > 0) {
					return -(i + 1);
				}
		    }
		    return -(segments.length + 1);
		}
	}

	public final class Segment {
	
		final UnsafeMemoryMappedFile mmf;
		final long startPageNum;
		final long segmentPages;
		
		Segment(UnsafeMemoryMappedFile mmf, long startPageNum, long segmentPages) {
			this.mmf = mmf;
			this.startPageNum = startPageNum;
			this.segmentPages = segmentPages;
		}
		
		long getAddress(long pageNum) {
			Requires.positive(pageNum, "pageNum");
			Requires.less(pageNum, this.segmentPages, "pageNum");
			return mmf.getAddress() + pageNum * pageSize.getPageSize();
		}
		
		void close() {
			mmf.close();
		}
		
	}
	
}
