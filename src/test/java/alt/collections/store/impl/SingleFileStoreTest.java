package alt.collections.store.impl;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import alt.collections.store.FileStore;
import alt.collections.store.Stores;

/**
 * Single File Store Test
 * 
 * @author Albert Shift
 *
 */

public class SingleFileStoreTest {

	@Test
	public void test() throws Exception {
		
		FileStore fs = Stores.fileStore(8192).addFile("target/test.mmf", 16384).build();
		
		Assert.assertEquals(2, fs.getTotalPages());
		
		fs.close();
		
	}
	
	@After
	public void tearDown() {
		File file = new File("test.mmf");
		if (file.exists()) {
			file.delete();
		}
	}
	
}
