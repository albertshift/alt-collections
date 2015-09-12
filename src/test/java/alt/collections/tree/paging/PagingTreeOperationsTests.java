package alt.collections.tree.paging;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.store.FileStore;
import alt.collections.store.Stores;
import alt.collections.tree.paging.PagingTree;
import alt.collections.util.PageSize;

/**
 * Persistent Tree Operations Tests
 * 
 * @author Albert Shift
 *
 */

public class PagingTreeOperationsTests {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	//private MemStore store;
	private FileStore store;
	
	private PagingTree<String, String> ptree;
	
	@Before
	public void init() throws Exception {
		new File("target/tree.mmf").delete();
		
		//store = Stores.memStore().withSize(100 * pageSize.getPageSize()).build();
		store = Stores.fileStore().addFile("target/tree.mmf", 2 * pageSize.getPageSize()).build();
		
		ptree = new PagingTree<String, String>(store, "firstTree"   );

	}

	@Test
	public void testGet() throws Exception {

		Assert.assertNull(ptree.get("testGetKey"));
		
		ptree.put("testGetKey", "testGetValue");

		Assert.assertEquals("testGetValue", ptree.get("testGetKey"));
		
    }

	@Test
	public void testPut() throws Exception {

		Assert.assertNull(ptree.get("testPutKey"));
		
		Assert.assertNull(ptree.put("testPutKey", "testPutValue"));
		
		Assert.assertEquals("testPutValue", ptree.get("testPutKey"));

		Assert.assertEquals("testPutValue", ptree.put("testPutKey", "testPutReplaceValue"));

		Assert.assertEquals("testPutReplaceValue", ptree.get("testPutKey"));

	}
	
	@Test
	public void testPutIfAbsent() throws Exception {

		Assert.assertNull(ptree.get("testPutIfAbsentKey"));
		
		Assert.assertNull(ptree.putIfAbsent("testPutIfAbsentKey", "testPutIfAbsentValue"));

		Assert.assertEquals("testPutIfAbsentValue", ptree.get("testPutIfAbsentKey"));

		Assert.assertEquals("testPutIfAbsentValue", ptree.putIfAbsent("testPutIfAbsentKey", "testPutIfAbsentReplaceValue"));
		
		Assert.assertEquals("testPutIfAbsentValue", ptree.get("testPutIfAbsentKey"));
		
	}
	
	@Test
	public void testReplaceNotExists() throws Exception {

		Assert.assertNull(ptree.get("testReplaceNotExistsKey"));
		
		Assert.assertNull(ptree.replace("testReplaceNotExistsKey", "testReplaceNotExistsValue"));

		Assert.assertNull(ptree.get("testReplaceNotExistsKey"));
		
	}
	
	@Test
	public void testReplaceExists() throws Exception {

		Assert.assertNull(ptree.get("testReplaceExistsKey"));
		
		ptree.put("testReplaceExistsKey", "testReplaceExistsValue");
		
		Assert.assertEquals("testReplaceExistsValue", ptree.replace("testReplaceExistsKey", "testReplaceExistsNewValue"));

		Assert.assertEquals("testReplaceExistsNewValue", ptree.get("testReplaceExistsKey"));
		
	}
	
	@Test
	public void testCompareReplace() throws Exception {

		Assert.assertNull(ptree.get("testCompareReplaceKey"));

		ptree.put("testCompareReplaceKey", "testCompareReplaceValue");

		Assert.assertFalse(ptree.replace("testCompareReplaceKey", "testCompareReplaceValueNotFound", "testCompareReplaceNewValue"));

		Assert.assertTrue(ptree.replace("testCompareReplaceKey", "testCompareReplaceValue", "testCompareReplaceNewValue"));

	}	
	
	@Test
	public void testRemove() throws Exception {

		Assert.assertNull(ptree.get("testRemoveKey"));
		
		Assert.assertNull(ptree.remove("testRemoveKey"));
		
		Assert.assertNull(ptree.put("testRemoveKey", "testRemoveValue"));
		
		Assert.assertEquals("testRemoveValue", ptree.remove("testRemoveKey"));

	}
	
	@Test
	public void testCompareRemove() throws Exception {

		Assert.assertNull(ptree.get("testCompareRemoveKey"));

		ptree.put("testCompareRemoveKey", "testCompareRemoveValue");

		Assert.assertFalse(ptree.remove("testCompareRemoveKey", "testCompareRemoveValueNotFound"));

		Assert.assertTrue(ptree.remove("testCompareRemoveKey", "testCompareRemoveValue"));

	}
	
	@After
	public void free() throws Exception {
		//store.free();
		store.close();
	}
	
}
