package alt.collections.paging;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
 * Page UTF-8 Tests
 * 
 * @author Albert Shift
 *
 */

public class PageUtf8Tests {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore(pageSize.getPageSize()).withSize(pageSize.getPageSize()).build();
		//store = Stores.fileStore(pageSize.getPageSize()).addFile("target/blob.mmf", pageSize.getPageSize()).build();
	}
	
	@Test
	public void testHelloWorld() {

		String str = "Hello world";
		
		PageWriter writer = new PageWriter(store, 0);
		writer.writeString(str);
		
		PageReader reader = new PageReader(store, 0);
		String actual = reader.readString();
		
		Assert.assertEquals(str, actual);
		
	}
	
	@Test
	public void testAll() throws UnsupportedEncodingException {

		PageWriter writer = new PageWriter(store, 0);
		PageReader reader = new PageReader(store, 0);
		
		for (int i = 0; i != 65535; ++i) {
			
			char[] chars = new char[] { (char)i };
						
			String str = new String(chars);
			byte[] utf8 = str.getBytes("UTF-8");
			
			writer.reset();
			writer.writeString(str);
			int endPosition = writer.getPosition();
			
			reader.reset();
			reader.readVLong();
			
			int sizeInBytes = endPosition - reader.getPosition();
			int estimated = PageWriter.estimateUtf8Size(str, 0, str.length());
			
			Assert.assertEquals(sizeInBytes, estimated);
			
			byte[] actual = new byte[sizeInBytes];
			reader.readBytesTo(actual, 0, sizeInBytes);
			
			Assert.assertTrue(Arrays.equals(utf8, actual));
			
			reader.reset();
			String act = reader.readString();
			
			Assert.assertEquals(new String(utf8, "UTF-8").charAt(0), act.charAt(0));
		}
		
	}
	
	@After
	public void free() throws Exception {
		store.free();
		//store.close();
	}
	
}
