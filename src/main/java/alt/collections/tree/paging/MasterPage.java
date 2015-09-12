package alt.collections.tree.paging;

import alt.collections.concurrent.CriticalSection;
import alt.collections.concurrent.IntegerCas;
import alt.collections.concurrent.PageNumCas;
import alt.collections.concurrent.CriticalSection.Callback;
import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.paging.Paging;
import alt.collections.util.ThreadUtil;

/**
 * Master page is using as an entry point to the store.
 * 
 * Master Page:
 * 
 * [magic:char], [pageTail:innerRef], [nextPageNum:pageNum], [storeTail:pageNum], [rootTree:int], [heapSpace:heap]
 * 
 * Master Continue Page:
 * 
 * [magic:char], [pageTail:innerRef], [nextPageNum:pageNum], [heapSpace:heap]
 * 
 * 
 * @author Albert Shift
 *
 */

public final class MasterPage extends AbstractMasterPage implements PageHeapSpace, PageManager {

	private PageNumCas storeTail;
	private IntegerCas rootEntry;
	
	//private static final AtomicBoolean structed = new AtomicBoolean(false);
	
	public MasterPage(Paging paging, long pageNum, boolean afterCreation) {
		super(paging);
		
		//if (!structed.get()) {
		//	System.out.println("Warning: Inconsistent state in MasterPage creation");
		//}
		
		PageReader pageReader = new PageReader(paging, pageNum);
		
		this.address = pageReader.getAddress();
		
		this.magic = pageReader.readChar();
		
		if (magic != MagicCodes.MASTER.getMagic()) {
			throw new PagingTreeException("invalid magic in store " + Integer.toHexString(magic));
		}
		
		this.pageTail = pageReader.readInnerRefCas();
		this.nextPageNum = pageReader.readPageNumCas();
		
		this.storeTail = pageReader.readPageNumCas();
		this.rootEntry = pageReader.readIntegerCas();
		
		int tail = this.pageTail.getInnerRef();
		if (tail < pageReader.getPosition() || tail > paging.getPageSize()) {
			throw new PagingTreeException("invalid pageTail in the page " + tail);
		}

		this.heapSpacePos = pageReader.getPosition();
		
		if (afterCreation && tail != heapSpacePos) {
			throw new PagingTreeException("blank master page must have valid tail=" + tail + " for heapSpacePos = " + heapSpacePos);
		}

	}
	
	public static MasterPage concurrentGetOrCreate(final Paging paging) {
		
		final PageReader pageReader = new PageReader(paging, 0);
        char magic = pageReader.readChar();
		if (magic == MagicCodes.MASTER.getMagic()) {
			return new MasterPage(paging, 0, false);  
		}
        
		
		pageReader.seek(paging.getPageSize() - 4);
		IntegerCas semaphore = pageReader.readIntegerCas();
		
		Boolean created = new CriticalSection(semaphore, 0).execute(new Callback<Boolean>() {

			@Override
			public Boolean doInCriticalSection() {
				pageReader.reset();
				char magic = pageReader.readChar();
				if (magic == 0) {
					structBlank(paging);
					return true;
				}
				return false;
			}
			
		});
		
		//if (created) {
		//	structed.set(true);
		//}
		
		return new MasterPage(paging, 0, false);  

	}
	
	public static MasterPage getOrCreate(Paging paging) {
	
		PageReader pageReader = new PageReader(paging, 0);
		
		char magic = pageReader.readChar();
		if (magic == 0) {
			structBlank(paging);
		}
		
		return new MasterPage(paging, 0, magic == 0);  
	}
	
	public static void structBlank(Paging paging) {
		
		PageWriter pageWriter = new PageWriter(paging, 0);
		
		// magic
		pageWriter.skip(2); 

		// pageTail
		int pageTailPos = pageWriter.getPositionAndSkipInnerRef();
		
		// nextPageNum
		pageWriter.writePageNum(0);
		
		// storeTail (one page is used)
		pageWriter.writePageNum(1);
		
		// rootEntry
		pageWriter.writeInt(0);
		
		int tail = pageWriter.getPosition();
		pageWriter.seek(pageTailPos);
		pageWriter.writeInnerRef(tail);
		
		pageWriter.reset();
		pageWriter.writeChar(MagicCodes.MASTER.getMagic()); 

	}

	@Override
	public long allocatePage() {
		while(true) {
			long tail = storeTail.getPageNum();
			if (tail + 1 > paging.getTotalPages()) {
				throw new PagingTreeException("no space in pages");
			}
			if (storeTail.casPageNum(tail, tail + 1)) {
				return tail;
			}
			ThreadUtil.loopSleep();
		}
	}

	@Override
	public void freePage(long pageNum, boolean delay) {
		throw new IllegalStateException("unsupported operation");
	}
	
	public IntegerCas getRootEntry() {
		return rootEntry;
	}
	
}
