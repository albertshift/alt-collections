package alt.collections.tree.paging;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import alt.collections.store.MemStore;
import alt.collections.store.Stores;
import alt.collections.tree.paging.PagingTree;
import alt.collections.util.PageSize;

/**
 * Persistent Tree Tests
 * 
 * @author Albert Shift
 *
 */

public class PagingTreeTests {

	private static Random random = new Random(5);
	//private static ConcurrentRandom random = new ConcurrentRandom(5);

	private static final PageSize pageSize = PageSize.UNSAFE_PAGESIZE;
	
	private MemStore store;
	//private FileStore store;
	
	@Before
	public void init() throws Exception {
		store = Stores.memStore().withSize(100 * pageSize.getPageSize()).build();
		//store = Stores.fileStore().addFile("target/tree.mmf", 100 * pageSize.getPageSize()).build();
	}
	
	//@Test
	public void test() throws Exception {
		
		PagingTree ptree = new PagingTree(store, "firstTree"   );

		long t0 = System.currentTimeMillis();
		for (int k = 0; k != 100; ++k) {
		
			for (int i = 0; i != 10; ++i) {
				 ptree = new PagingTree(store, "firstTree" + i  );
			}
		
		}
		
		System.out.println("Time = " + (System.currentTimeMillis() - t0) );
		
		
		
	}
	
	@Test
	public void testParallel() throws Exception {
		
		//PersistentTree ptree = new PersistentTree(store, "firstTree"   );

		
		int numThreads = 20;
		
		CyclicBarrier barrier = new CyclicBarrier(numThreads);
		
		//ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		
		Thread[] threads = new Thread[numThreads];
		Worker[] workers = new Worker[numThreads];
		for (int i = 0; i != numThreads; ++i) {
			workers[i] = new Worker(barrier);
			//executor.execute(workers[i]);
			threads[i] = new Thread(workers[i]);
			threads[i].start();
		}
		
		long t0 = System.currentTimeMillis();
		
		for (int i = 0; i != numThreads; ++i) {
			threads[i].join();
		}
		
		//Thread.sleep(30000);
		
		//executor.shutdown();
		//executor.awaitTermination(30, TimeUnit.SECONDS);
		
		System.out.println("Time = " + (System.currentTimeMillis() - t0) );
		
		/*
		TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
		for (int i = 0; i != threads; ++i) {
			//System.out.println("Worker " + i + ", treeName = " + workers[i].getTreeName());
			Integer old = tm.put(workers[i].getTreeName(), i);
			if (old != null) {
				System.out.println("Duplicate old = " + old  + ", new = " + i + ", treeName = " + workers[i].getTreeName());
			}

		}
		*/
		
		for (int i = 0; i != numThreads; ++i) {
			Thread thread = threads[i];
			System.out.println("Worker " + i + ", thread = " + thread.isAlive());
			
			StackTraceElement[] stack = thread.getStackTrace();
			for (int j = 0; j != 10 && j != stack.length; ++j) {
				System.out.println("      " + stack[j]);
			}
		}
		
	}
	
	public class Worker implements Runnable {

		private final CyclicBarrier barrier;
		private String treeName;
		private PagingTree ptree;
		
		public Worker(CyclicBarrier barrier) {
			this.barrier = barrier;
		}
		
		@Override
		public void run() {
			
			try {
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i != 100; ++i) {
				treeName = "firstTree" + getNextKey();
				ptree = new PagingTree(store, treeName );
			}

			
		}

		public String getTreeName() {
			return treeName;
		}

		public PagingTree getPtree() {
			return ptree;
		}

		
	}
	
	private String getBigString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i != 4096; ++i) {
			str.append('a');
		}
		return str.toString();
	}
	
	@After
	public void free() throws Exception {
		store.free();
		//store.close();
	}
	
	private int getNextKey() {
		return (random.nextInt() >>> 1) % 1000;
	}
	
}
