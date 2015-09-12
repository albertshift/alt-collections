package alt.collections.util;


import org.junit.Assert;
import org.junit.Test;

import alt.collections.util.PageSize;

/**
 * 
 * @author Albert Shift
 *
 */

public class PageSizeTest {

	private PageSize memoryPageSize = PageSize.UNSAFE_PAGESIZE;
	
	@Test
	public void testIsAligned() {
		
		Assert.assertFalse(memoryPageSize.isAligned(123L));
		Assert.assertFalse(memoryPageSize.isAligned(1234L));
	
		Assert.assertTrue(memoryPageSize.isAligned(0L));
		Assert.assertTrue(memoryPageSize.isAligned(4096L));
		Assert.assertTrue(memoryPageSize.isAligned(8192L));
	}
	
	@Test
	public void testTopAlign() {
		
		Assert.assertEquals(0L, memoryPageSize.alignTop(0L));
		Assert.assertEquals(4096L, memoryPageSize.alignTop(4096L));
		Assert.assertEquals(4096L, memoryPageSize.alignTop(4097L));
		Assert.assertEquals(4096L, memoryPageSize.alignTop(8191L));
		
	}
	
	@Test
	public void testBottomAlign() {
		
		Assert.assertEquals(0L, memoryPageSize.alignBottom(0L));
		Assert.assertEquals(4096L, memoryPageSize.alignBottom(4096L));
		Assert.assertEquals(8192L, memoryPageSize.alignBottom(4097L));
		Assert.assertEquals(8192L, memoryPageSize.alignBottom(8191L));
		
	}
}
