package alt.collections.tree.paging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;

import org.junit.After;
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

public class PagingTreeGraphTests {

	private static Random random = new Random();
	
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

		for (int i = 0; i != 10; ++i) {
			ptree.put("Key" + i, "Value" + i);
		}
		
		//ptree.put("5", "A");
		//ptree.put("1", "B");
		//ptree.put("7", "C");
		//ptree.put("6", "D");

		PrintStream ps = new PrintStream(new FileOutputStream("leaf.dg"));
		
		try {
			ptree.printGraph(ps);
		}
		finally {
			ps.close();
		}
		
		
    }

	
	@After
	public void free() throws Exception {
		//store.free();
		store.close();
	}
	
}
