package alt.collections.concurrent;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import alt.collections.concurrent.CriticalSection;
import alt.collections.concurrent.IntegerCas;
import alt.collections.concurrent.CriticalSection.Callback;
import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.paging.Paging;
import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.util.PageSize;

/**
 * Critical Section Tests
 * 
 * @author Albert Shift
 *
 */

public class CriticalSectionTests {

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore().withSize(pageSize.getPageSize()).build();
	}
	
	@Test
	public void testParallel() throws Exception {
		
		SharedResource sharedResource = new SharedResource(store);
		
		//sharedResource.touch();

		long t0 = System.currentTimeMillis();
		
		int threads = 100;
		
		CyclicBarrier barrier = new CyclicBarrier(threads);
		
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		
		Worker[] workers = new Worker[threads];
		for (int i = 0; i != threads; ++i) {
			workers[i] = new Worker(barrier, sharedResource);
			executor.execute(workers[i]);
		}
		
		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);
		
		System.out.println("Time = " + (System.currentTimeMillis() - t0) );
		
	}
	
	public class Worker implements Runnable {

		private final CyclicBarrier barrier;
		private final SharedResource sharedResource;
		
		public Worker(CyclicBarrier barrier, SharedResource sharedResource) {
			this.barrier = barrier;
			this.sharedResource = sharedResource;
		}
		
		@Override
		public void run() {
			
			try {
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i != 100; ++i) {
				sharedResource.touch();
			}

			
		}
		
	}
	
	public static class SharedResource {
		
		private final Paging paging;
		private final long pageNum;
		
		public SharedResource(Paging paging) {
			this.paging = paging;
			this.pageNum = 0;
		}
		
		public void touch() {
			
			PageReader reader = new PageReader(paging, pageNum);
			reader.seek(paging.getPageSize() - 4);
			IntegerCas spinCas = reader.readIntegerCas(); 
			CriticalSection cs = new CriticalSection(spinCas, 0);
			cs.<Object>execute(new Callback<Object>() {

				@Override
				public Object doInCriticalSection() {
					format();
					return null;
				}
				
			});
		}

		private void format() {
			PageReader reader = new PageReader(paging, pageNum);
			char magic = reader.readChar();
			if (magic != 'A') {
				PageWriter writer = new PageWriter(paging, pageNum);
				writer.writeChar('A');
			}
		}
		
	}

	
	@After
	public void free() throws Exception {
		store.free();
	}
}
