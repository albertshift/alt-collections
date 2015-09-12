package alt.collections.store.impl;



import org.junit.Assert;
import org.junit.Test;

import alt.collections.store.FileStore;
import alt.collections.store.Stores;

/**
 * 
 * @author Albert Shift
 *
 */
public class MultipleFileStoreTest {

	@Test
	public void test() throws Exception {
		
		FileStore fs = Stores.fileStore(4096).addFiles("target/folder%s/segment%s.mmf", 1500, 4096).deleteOnExit().build();
		
		Assert.assertEquals(1500, fs.getTotalPages());
		
	}

}
