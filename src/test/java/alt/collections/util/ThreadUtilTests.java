package alt.collections.util;

import org.junit.Test;

import alt.collections.util.ThreadUtil;

/**
 * Thread Util Tests
 * 
 * @author Albert Shift
 *
 */

public class ThreadUtilTests {

	@Test
	public void testJni() {
		
		System.out.println("JniThreadUtil lib loaded " + ThreadUtil.isJniLoaded());
		
		ThreadUtil.usleep(250);
	}
	
	
	
}
