package alt.collections.tree.paging;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.tree.paging.NamedTreeImmutableMap;
import alt.collections.util.PageSize;

/**
 * Root Tree Tests
 * 
 * @author Albert Shift
 *
 */

public class NameTreeTests {

	private static Random random = new Random(5);

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	//private FileStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore().withSize(100 * pageSize.getPageSize()).build();
		//store = Stores.fileStore().addFile("target/tree.mmf", 100 * pageSize.getPageSize()).build();
	}
	
	@Test
	public void test() throws Exception {
		
		NamedTreeImmutableMap nameTree = new NamedTreeImmutableMap(store);
				
		for (int i = 0; i != 100; ++i) {
			nameTree.findOrCreate("alex" + i);
			
			Assert.assertNotNull(nameTree.find("alex" + i));
		}

		Assert.assertNull(nameTree.find("alex"));

		List<String> names = nameTree.getAllNames();
		
		System.out.println("names = " + names);
		
	}
	
	@After
	public void free() throws Exception {
		store.free();
		//store.close();
	}
	
	private int getNextKey() {
		return random.nextInt(1000);
	}
	
}
