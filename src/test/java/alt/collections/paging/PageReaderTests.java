package alt.collections.paging;



import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.paging.PageReader;
import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.util.PageSize;
import alt.collections.util.Unsafe;

/**
 * Page Reader Tests
 * 
 * @author Albert Shift
 *
 */

public class PageReaderTests {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore(pageSize.getPageSize()).withSize(pageSize.getPageSize()).build();
	}
	
	@Test
	public void testPositive() throws Exception {

		long address = store.getAddress(0);
		
		PageReader reader = new PageReader(store, 0);

		for (int i = 0; i != 8; ++i) {
			Unsafe.INSTANCE.putByte(address + i, (byte) (i + 1) );
		}
		
		Assert.assertEquals(1,  reader.readByte());
		reader.reset();
		
		Assert.assertEquals(0x201,  reader.readChar());
		reader.reset();
		
		Assert.assertEquals(0x201,  reader.readShort());
		reader.reset();
		
		Assert.assertEquals(0x4030201,  reader.readInt());
		reader.reset();
		
		Assert.assertEquals(0x807060504030201L,  reader.readLong());
		
	}
	
	@Test
	public void testNegative() throws Exception {

		long address = store.getAddress(0);
		
		PageReader reader = new PageReader(store, 0);

		for (int i = 0; i != 8; ++i) {
			Unsafe.INSTANCE.putByte(address + i, (byte) (128 + i) );
		}
		
		Assert.assertEquals(-128,  reader.readByte());
		reader.reset();
		
		Assert.assertEquals(33152,  reader.readChar());
		reader.reset();
		
		Assert.assertEquals(-32384,  reader.readShort());
		reader.reset();
		
		Assert.assertEquals(-2088599168,  reader.readInt());
		reader.reset();
		
		Assert.assertEquals(-8681104427521506944L,  reader.readLong());
		
	}
	
	@After
	public void free() throws Exception {
		store.free();
	}
	
}
