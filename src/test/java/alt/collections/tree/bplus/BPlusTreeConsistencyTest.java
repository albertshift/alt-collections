package alt.collections.tree.bplus;

import java.util.Random;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import alt.collections.tree.Comparators;
import alt.collections.tree.Entries;
import alt.collections.tree.Tree;
import alt.collections.tree.bplus.BPlusTree;

/**
 * Consistency test for the B-Tree 
 * 
 * @author Albert Shift
 *
 */

public class BPlusTreeConsistencyTest {

	private static Random random = new Random(5);
	
	private enum Operation {
		GET, PUT, REMOVE;
	}
	
	private Operation op;
	private int key;
	
	@Test
	public void test() {
		
		TreeMap<Integer, Integer> controlMap = new TreeMap<Integer, Integer>();
		
		BPlusTree<Integer, Integer> testMap = BPlusTree.newInstance(3, Comparators.INTEGER);
		//HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();
		
		try {
			testLoop(controlMap, testMap);
		}
		catch(IllegalStateException e) {
			System.out.println("Last operation " + this.op + ", last key = " + this.key);
			e.printStackTrace();
			testMap.print();
		}
		
		Assert.assertEquals(controlMap.size(), testMap.size());
	}
	
	private void testLoop(TreeMap<Integer, Integer> controlMap, BPlusTree<Integer, Integer> testMap) {
		for (int i = 0; i != 1000000; ++i) {
			this.key = getNextKey();
			this.op = getRandomOperation();
			Integer controlValue = null;
			Tree.Entry<Integer, Integer> testEntry = null;
			switch(this.op) {
			case GET:
				controlValue = controlMap.get(this.key);
				testEntry = testMap.get(this.key);
				Assert.assertEquals(controlValue, testEntry != null ? testEntry.getValue() : null);
				testMap.verify();
				break;
			case PUT:
				controlValue = controlMap.put(this.key, this.key);
				testEntry = testMap.put(Entries.simple(this.key, this.key));
				Assert.assertEquals(controlValue, testEntry != null ? testEntry.getValue() : null);
				testMap.verify();
				break;
			case REMOVE:
				controlValue = controlMap.remove(this.key);
				testEntry = testMap.remove(this.key);
				Assert.assertEquals(controlValue, testEntry != null ? testEntry.getValue() : null);
				testMap.verify();
				break;
			}
		}
	}

	private int getNextKey() {
		return random.nextInt(1000);
	}
	
	private Operation getRandomOperation() {
		int ordinal = random.nextInt(3);
		return Operation.values()[ordinal];
	}
	
}
