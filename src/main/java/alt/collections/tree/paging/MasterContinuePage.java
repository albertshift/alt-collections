package alt.collections.tree.paging;

import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.paging.Paging;

/**
 * Master page is using as an entry point to the store.
 * 
 * Master Page:
 * 
 * [magic:char], [pageTail:innerRef], [nextPageNum:pageNum], [storeTail:pageNum], [heapSpace:heap]
 * 
 * Master Continue Page:
 * 
 * [magic:char], [pageTail:innerRef], [nextPageNum:pageNum], [heapSpace:heap]
 * 
 * 
 * @author Albert Shift
 *
 */

public final class MasterContinuePage extends AbstractMasterPage implements PageHeapSpace {

	public MasterContinuePage(Paging paging, long pageNum, boolean afterCreation) {
		super(paging);
		
		PageReader pageReader = new PageReader(paging, pageNum);
		
		this.address = pageReader.getAddress();
		
		this.magic = pageReader.readChar();
		
		if (magic != MagicCodes.MASTER_CONTINUE.getMagic()) {
			throw new PagingTreeException("invalid magic in store " + Integer.toHexString(magic));
		}
		
		this.pageTail = pageReader.readInnerRefCas();

		this.nextPageNum = pageReader.readPageNumCas();
		
		int tail = this.pageTail.getInnerRef();
		if (tail < pageReader.getPosition() || tail > paging.getPageSize()) {
			throw new PagingTreeException("invalid pageTail in the page " + tail);
		}

		this.heapSpacePos = pageReader.getPosition();
		
		if (afterCreation && tail != heapSpacePos) {
			throw new PagingTreeException("blank master page must have valid tail=" + tail + " for heapSpacePos = " + heapSpacePos);
		}

	}
	
	public static void structBlank(Paging paging, long pageNum) {
		
		PageWriter pageWriter = new PageWriter(paging, pageNum);
		
		// magic
		pageWriter.writeChar(MagicCodes.MASTER_CONTINUE.getMagic()); 

		// pageTail
		int pageTailPos = pageWriter.getPositionAndSkipInnerRef();
		
		// nextPageNum
		pageWriter.writePageNum(0);
		
		int tail = pageWriter.getPosition();
		pageWriter.seek(pageTailPos);
		pageWriter.writeInnerRef(tail);

	}

}
