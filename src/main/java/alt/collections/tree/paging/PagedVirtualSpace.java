package alt.collections.tree.paging;

import java.util.ArrayList;
import java.util.List;

import alt.collections.concurrent.PageNumCas;
import alt.collections.paging.PageStream;
import alt.collections.paging.Paging;

/**
 * Paged Virtual Space is used to store Root Tree
 * 
 * @author Albert Shift
 *
 */

public final class PagedVirtualSpace implements VirtualSpace {

	private final int pageSize;
	
	private final PageManager pageManager;
	
	private final List<PageHeapSpace> pages = new ArrayList<PageHeapSpace>();;

	public PagedVirtualSpace(Paging paging, PageManager pageManager, PageHeapSpace master) {
		this.pageSize = paging.getPageSize();
		this.pageManager = pageManager;
		this.pages.add(master);
	}
	
	private boolean loadNextPage() {
		
		PageHeapSpace lastPage = getLastPage();
		
		PageNumCas nextPageCas = lastPage.getNextPageNum();
		
		long nextPageNum = nextPageCas.getPageNum();
		if (nextPageNum != 0) {
			pages.add(lastPage.getNextPage(nextPageNum));
			return true;
		}
		
		return false;
	}
	
	private void createNewPage() {
		
		long newPage = pageManager.allocatePage();
		
		PageHeapSpace lastPage = getLastPage();

		PageHeapSpace createdPage = lastPage.structNextPage(newPage);

		while(true) {
			
			PageNumCas nextPageCas = lastPage.getNextPageNum();
			long nextPageNum = nextPageCas.getPageNum();
			if (nextPageNum != 0) {
				lastPage = lastPage.getNextPage(nextPageNum);
				pages.add(lastPage);
			}
			else if (nextPageCas.casPageNum(0, newPage)) {
				pages.add(createdPage);
				return;
			}
			
		}

	}
	
	private PageHeapSpace getLastPage() {
		return this.pages.get(pages.size() - 1);
	}
	
	@Override
	public int allocate(int size) {
		return allocateSpace(size, true, false);
	}
	
	private int allocateSpace(int size, boolean lookInAll, boolean justCreatedNewPage) {
		
		if (lookInAll) {
			for (int i = 0; i != pages.size(); ++i) {
				int innerRef = pages.get(i).tryAllocate(size);
				if (innerRef != -1) {
					return innerRef + i * pageSize;
				}
			}
		}
		else {
			// look in last page
			int i = pages.size() - 1;
			int innerRef = pages.get(i).tryAllocate(size);
			if (innerRef != -1) {
				return innerRef + i * pageSize;
			}
		}

		if (loadNextPage()) {
			return allocateSpace(size, false, justCreatedNewPage);
		}
		
		if (justCreatedNewPage) {
			throw new IllegalArgumentException("too big space requested " + size);
		}
		
		createNewPage();
		return allocateSpace(size, false, true);

	}

	@Override
	public void seek(PageStream stream, int virtualRef) {
		int pageInList = virtualRef / pageSize;
		
		while (pageInList >= pages.size()) {
			if (!loadNextPage()) {
				throw new IndexOutOfBoundsException("invalid virtualRef = " + virtualRef + ", pages = " + pages.size());
			}
		}
		
		PageHeapSpace page = pages.get(pageInList);
		
		stream.switchAddress(page.getAddress());
		stream.seek(virtualRef % pageSize);
	}

}
