package alt.collections.paging;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.util.PageSize;

/**
 * Page VLong Tests
 * 
 * @author Albert Shift
 *
 */

public class PageVLongTests {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore(pageSize.getPageSize()).withSize(pageSize.getPageSize()).build();
	}
	
	@Test
	public void test() throws Exception {
		Assert.assertEquals(0L, serializeAndDeserialize(0L));
		Assert.assertEquals(123L, serializeAndDeserialize(123L));
		Assert.assertEquals(-123L, serializeAndDeserialize(-123L));
		Assert.assertEquals(Integer.MAX_VALUE, serializeAndDeserialize(Integer.MAX_VALUE));
		Assert.assertEquals(Integer.MIN_VALUE, serializeAndDeserialize(Integer.MIN_VALUE));
		Assert.assertEquals(Long.MAX_VALUE, serializeAndDeserialize(Long.MAX_VALUE));
		Assert.assertEquals(Long.MIN_VALUE, serializeAndDeserialize(Long.MIN_VALUE));
	}
	
	private long serializeAndDeserialize(long expectedValue) throws IOException {
		PageWriter writer = new PageWriter(store, 0);
		writer.writeVLong(expectedValue);
		PageReader reader = new PageReader(store, 0);
		return reader.readVLong();
	}
	
	@After
	public void free() throws Exception {
		store.free();
	}
	
}
