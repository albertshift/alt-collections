package alt.collections.util;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import alt.collections.util.MapFileMode;
import alt.collections.util.UnsafeMemoryMappedFile;

/**
 * 
 * @author Albert Shift
 *
 */
public class UnsafeMemoryMappedFileTest {

	@Test
	public void testTwoGbFile() throws Exception {
		
		UnsafeMemoryMappedFile mmf = new UnsafeMemoryMappedFile("test.mmf", MapFileMode.READ_WRITE, 8192);

		System.out.println("Size = " + mmf.getSize());
		
		mmf.close();
		
	}
	
	@After
	public void tearDown() {
		File file = new File("test.mmf");
		if (file.exists()) {
			file.delete();
		}
	}
}
