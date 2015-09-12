package alt.collections.store.impl;

import org.junit.Test;

/**
 * Bubble Sort Tests
 * 
 * @author Albert Shift
 *
 */

public class BubbleSortTests {

	@Test
	public void test() {
		
		//System.out.println("Bubble Sort");
		
		int[] array = new int[] { 50, 3, 11, 102, 66, 2, 1 };
		
		//System.out.println("start = " + Arrays.toString(array));
		
		for (int i = 0; i != array.length-1; ++i) {
			
			for (int j = 0; j != array.length-i-1; ++j) {
				int element = array[j];
				int toCompare = array[j+1];
				
				if (element > toCompare) {
					array[j] = toCompare;
					array[j+1] = element;
				}
				
				//System.out.println("   " + Arrays.toString(array));
			}
			
			//System.out.println("" + i + ": " + Arrays.toString(array));
		}
		
		//System.out.println("final = " + Arrays.toString(array));
		
	}
	
}
