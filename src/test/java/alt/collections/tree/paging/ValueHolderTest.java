package alt.collections.tree.paging;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.store.FileStore;
import alt.collections.store.Stores;
import alt.collections.tree.paging.MutableLong;
import alt.collections.tree.paging.ValueHolder;
import alt.collections.util.PageSize;

/**
 * Value Holder Tests
 * 
 * @author Albert Shift
 *
 */

public class ValueHolderTest {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	//private MemStore store;
	private FileStore store;
	
	@Before
	public void init() throws Exception {
		new File("target/value-holder.mmf").delete();

		//store = Stores.memStore().withSize(100 * pageSize.getPageSize()).build();
		store = Stores.fileStore().addFile("target/value-holder.mmf", pageSize.getPageSize()).build();
	}
	
	
	@Test
	public void testString() throws Exception {

		String str = "Hello World!";
		doTest(str, 1 + 1 + str.length());
		
		PageReader pageReader = new PageReader(store, 0);
		int c = ValueHolder.compareTo(pageReader, "Hello World");
		Assert.assertTrue(c == 1);
		
		pageReader.reset();
		c = ValueHolder.compareTo(pageReader, "Hello World!!");
		Assert.assertTrue(c == -1);
		
		pageReader.reset();
		c = ValueHolder.compareTo(pageReader, "Hella World!");
		Assert.assertTrue(c > 0);
		
		pageReader.reset();
		c = ValueHolder.compareTo(pageReader, "Hello Yorld!");
		Assert.assertTrue(c < 0);
		
	}
	
	@Test
	public void testLong() throws Exception {

		doTest(new Long(12), 1 + 1);
		doTest(new Long(123), 1 + 2);
		doTest(new Long(12345), 1 + 3);
		
	}
	
	@Test
	public void testMutableLong() throws Exception {

		doTest(new MutableLong(12), 1 + 8);
		doTest(new MutableLong(123), 1 + 8);
		doTest(new MutableLong(12345), 1 + 8);
		
	}
	
	@Test
	public void testBlob() throws Exception {

		byte[] blob = "Hello World!".getBytes();
		
		doTest(blob, 1 + 1 + blob.length);

	}
	
	private void doTest(Object value, int expectedSize) {
		
		int size = ValueHolder.estimateSize(value);
		
		Assert.assertEquals(expectedSize, size);
		
		PageWriter pageWriter = new PageWriter(store, 0);
		ValueHolder.writeValue(pageWriter, value);
		
		PageReader pageReader = new PageReader(store, 0);
		Object actual = ValueHolder.readValue(pageReader);
		
		if (value instanceof MutableLong) {
			Assert.assertEquals(((MutableLong)value).longValue(), ((MutableLong)actual).longValue());
		}
		else if (value instanceof byte[]) {
			Assert.assertArrayEquals((byte[])value, (byte[])actual);
		}
		else {
			Assert.assertEquals(value, actual);
		}
		
		// test comparator
		pageReader.reset();
		int c = ValueHolder.compareTo(pageReader, value);
		Assert.assertTrue(c == 0);
		
	}
	
	@After
	public void free() throws Exception {
		//store.free();
		store.close();
	}
	
}
