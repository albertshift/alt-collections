package alt.collections.tree.b2;

import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import alt.collections.tree.Comparators;
import alt.collections.tree.SimpleEntry;
import alt.collections.tree.Tree;
import alt.collections.tree.b2.B2Tree;

/**
 * BTree Performance Tests
 * 
 * @author Albert Shift
 *
 */

public class BTreePerformanceTests {

	public static final int N = 1000000;
	
	@Test
	public void performanceSequentialTest() throws InterruptedException {
		System.out.println("Sequential Test");
		
		run(new ValueFunction() {

			@Override
			public Integer apply(int i) {
				return i;
			}
			
		});
	}
	
	@Test
	public void performanceRandomTest() throws InterruptedException {
		Random random = new Random();
		
		final int a = random.nextInt();
		final int b = random.nextInt();
		
		int x = gcd(a, b);
		if (Math.abs(x) != 1) {
			 performanceRandomTest();
			 return;
		}

		System.out.println("Random Test");
		
		run(new ValueFunction() {

			@Override
			public Integer apply(int i) {
				return i * a + b;
			}
			
		});
	}
	
    public static int gcd(int a, int b) {
        if (b == 0) return a;
        int x = a % b;
        return gcd(b, x);
    }
		
	public interface ValueFunction {
		Integer apply(int i);
	}
	
	private void run(ValueFunction func) throws InterruptedException {	
		System.gc();
		Thread.currentThread().sleep(1000);
		
		HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
		
		long t0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			hashMap.put(i, func.apply(i));
		}
		long td = System.currentTimeMillis() - t0;
		
		System.out.println("hashmap puts = " + td);

		t0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			Assert.assertEquals(func.apply(i), hashMap.get(i));
		}
		td = System.currentTimeMillis() - t0;
		
		System.out.println("hashmap gets = " + td);

		System.out.println("hashmap size = " + hashMap.size());
		
		hashMap.clear();
		System.gc();
		Thread.currentThread().sleep(1000);
		
		TreeMap<Integer, Integer> treeMap = new TreeMap<Integer, Integer>();

		t0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			treeMap.put(i, func.apply(i));
		}
		td = System.currentTimeMillis() - t0;
		
		System.out.println("treeMap puts = " + td);

		t0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			Assert.assertEquals(func.apply(i), treeMap.get(i));
		}
		td = System.currentTimeMillis() - t0;
		
		System.out.println("treeMap gets = " + td);
		
		System.out.println("treeMap size = " + treeMap.size());

		treeMap.clear();
		System.gc();
		Thread.currentThread().sleep(1000);

		
		B2Tree<Integer, Integer> btree = B2Tree.newInstance(1000, Comparators.INTEGER);
        
		t0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			btree.put(SimpleEntry.newEntry(i, func.apply(i)));
		}
		td = System.currentTimeMillis() - t0;
		
		System.out.println("btree puts = " + td);

		t0 = System.currentTimeMillis();
		for (int i = 0; i != N; ++i) {
			Tree.Entry<Integer, Integer> entry = btree.get(i);
			Assert.assertEquals(func.apply(i), entry != null ? entry.getValue() : null);
		}
		td = System.currentTimeMillis() - t0;
		
		System.out.println("btree gets = " + td);

		System.out.println("btree size = " + btree.size());
		
		btree.clear();
		//System.gc();
		//Thread.currentThread().sleep(1000);

		
	}
	
}
