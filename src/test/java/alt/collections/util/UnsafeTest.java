package alt.collections.util;

import org.junit.Test;

import alt.collections.util.Unsafe;

/**
 * 
 * @author Albert Shift
 *
 */

public class UnsafeTest {

	@Test
	public void test() {
		
		long pageSize = Unsafe.INSTANCE.pageSize();
		System.out.println("pageSize = " + pageSize);
		
	}
	
}
