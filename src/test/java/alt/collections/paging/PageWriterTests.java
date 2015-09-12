package alt.collections.paging;



import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.paging.PageWriter;
import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.util.PageSize;
import alt.collections.util.Unsafe;

/**
 * Page Writer Tests
 * 
 * @author Albert Shift
 *
 */

public class PageWriterTests {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore(pageSize.getPageSize()).withSize(pageSize.getPageSize()).build();
	}
	
	@Test
	public void test() throws Exception {

		PageWriter writer = new PageWriter(store, 0);

		long address = writer.getAddress();
		
		writer.writeByte((byte)-1);
		writer.reset();
		
		Assert.assertEquals(-1,  Unsafe.INSTANCE.getByte(address));
		
		writer.writeChar((char) 65535);
		writer.reset();
		
		Assert.assertEquals(65535,  Unsafe.INSTANCE.getChar(address));

		writer.writeInt((char) 65535);
		writer.reset();
		
		Assert.assertEquals(65535,  Unsafe.INSTANCE.getChar(address));

	}
	
	@After
	public void free() throws Exception {
		store.free();
	}
	
}
