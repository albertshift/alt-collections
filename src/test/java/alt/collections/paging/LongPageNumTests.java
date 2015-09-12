package alt.collections.paging;



import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.paging.LongPageNum;
import alt.collections.paging.PageNum;
import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.util.PageSize;

/**
 * Long Page Num Tests
 * 
 * @author Albert Shift
 *
 */

public class LongPageNumTests {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore(pageSize.getPageSize()).withSize(pageSize.getPageSize()).build();
	}
	
	@Test
	public void test() throws Exception {
		Assert.assertEquals(0L, writeAndRead(0L));
		Assert.assertEquals(123L, writeAndRead(123L));
		Assert.assertEquals(-123L, writeAndRead(-123L));
		Assert.assertEquals(Integer.MAX_VALUE, writeAndRead(Integer.MAX_VALUE));
		Assert.assertEquals(Integer.MIN_VALUE, writeAndRead(Integer.MIN_VALUE));
		Assert.assertEquals(Long.MAX_VALUE, writeAndRead(Long.MAX_VALUE));
		Assert.assertEquals(Long.MIN_VALUE, writeAndRead(Long.MIN_VALUE));
	}
	
	@Test
	public void testCas() throws Exception {
		
		long[] experiment = new long[] {0L, 123L, -123L, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE };
		
		CyclicBarrier barrier = new CyclicBarrier(experiment.length);
		
		ExecutorService executor = Executors.newFixedThreadPool(experiment.length);
		
		Worker[] workers = new Worker[experiment.length];
		for (int i = 0; i != experiment.length; ++i) {
			workers[i] = new Worker(barrier, experiment[i]);
			executor.execute(workers[i]);
		}
		
		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);
		
		for (int i = 0; i != experiment.length; ++i) {
			workers[i].assertEquals();
		}
		
	}
	
	public class Worker implements Runnable {

		private final CyclicBarrier barrier;
		private final long testValue;
		private long resultValue;
		
		public Worker(CyclicBarrier barrier, long testValue) {
			this.barrier = barrier;
			this.testValue = testValue;
		}
		
		@Override
		public void run() {
			
			try {
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i != 100; ++i) {
				resultValue = writeAndReadCas(testValue);
			}
			
		}
		
		public void assertEquals() {
			Assert.assertEquals(testValue, resultValue);
		}
		
	}
	
	private long writeAndRead(long value) {
		
		PageNum pageNum = new LongPageNum();
		
		long address = store.getAddress(0);
		
		pageNum.writePageNum(address, value);
		
		return pageNum.readPageNum(address);
	}
	
	private long writeAndReadCas(long value) {
		
		PageNum pageNum = new LongPageNum();
		
		long address = store.getAddress(0);
		
		while(true) {
			
			long old = pageNum.readPageNum(address);
			
			if (pageNum.casPageNum(address, old, value)) {
				break;
			}
		}
		
		return pageNum.readPageNum(address);
	}
	
	@After
	public void free() throws Exception {
		store.free();
	}
}
