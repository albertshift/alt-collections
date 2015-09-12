package alt.collections.paging;

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
 * Page Blob Tests
 * 
 * @author Albert Shift
 *
 */

public class PageBytesTests {


	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore(pageSize.getPageSize()).withSize(pageSize.getPageSize()).build();
		//store = Stores.fileStore(pageSize.getPageSize()).addFile("target/blob.mmf", pageSize.getPageSize()).build();
	}
	
	@Test
	public void test() throws Exception {

		String str = "Hello world";
		
		byte[] expected = str.getBytes("UTF-8");
		
		PageWriter writer = new PageWriter(store, 0);
		writer.writeBytes(expected);
		
		PageReader reader = new PageReader(store, 0);
		byte[] blob = reader.readBytes();
		
		String actual = new String(blob, "UTF-8");
		
		Assert.assertArrayEquals(expected, blob);
		Assert.assertEquals(str, actual);
		
	}
	
	
	@After
	public void free() throws Exception {
		store.free();
		//store.close();
	}
	
}
