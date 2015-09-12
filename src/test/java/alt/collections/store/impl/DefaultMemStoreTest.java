package alt.collections.store.impl;

import org.junit.Assert;
import org.junit.Test;

import alt.collections.store.MemStore;
import alt.collections.store.Stores;

/**
 * Default Mem Store Test
 * 
 * @author Albert Shift
 *
 */
public class DefaultMemStoreTest {

	@Test
	public void test() throws Exception {
		
		MemStore fs = Stores.memStore(8192).withSize(16384).build();
		
		Assert.assertEquals(2, fs.getTotalPages());
		
		fs.free();
		
	}
	
}
