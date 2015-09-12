package alt.collections.paging;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import alt.collections.paging.InnerRef;
import alt.collections.paging.IntegerInnerRef;
import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.util.PageSize;

/**
 * Integer Inner Ref Tests
 * 
 * @author Albert Shift
 *
 */

public class IntegerInnerRefTests {


	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore(pageSize.getPageSize()).withSize(pageSize.getPageSize()).build();
	}
	
	@Test
	public void test() throws Exception {
		Assert.assertEquals(0, writeAndRead(0));
		Assert.assertEquals(123, writeAndRead(123));
		Assert.assertEquals(-123, writeAndRead(-123));
		Assert.assertEquals(Integer.MAX_VALUE, writeAndRead(Integer.MAX_VALUE));
		Assert.assertEquals(Integer.MIN_VALUE, writeAndRead(Integer.MIN_VALUE));
	}
	
	@Test
	public void testCas() throws Exception {
		
		int[] experiment = new int[] {0, 123, -123, Integer.MAX_VALUE, Integer.MIN_VALUE };
		
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
		private final int testValue;
		private int resultValue;
		
		public Worker(CyclicBarrier barrier, int testValue) {
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
	
	private int writeAndRead(int value) {
		
		InnerRef innerRef = new IntegerInnerRef();
		
		long address = store.getAddress(0);
		
		innerRef.writeInnerRef(address, value);
		
		return innerRef.readInnerRef(address);
	}
	
	private int writeAndReadCas(int value) {
		
		InnerRef innerRef = new IntegerInnerRef();
		
		long address = store.getAddress(0);
		
		while(true) {
			
			int old = innerRef.readInnerRef(address);
			
			if (innerRef.casInnerRef(address, old, value)) {
				break;
			}
		}
		
		return innerRef.readInnerRef(address);
	}
	
	@After
	public void free() throws Exception {
		store.free();
	}
	
}
