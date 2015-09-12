package alt.collections.tree.bplus;

import java.util.Comparator;

/**
 * Configuration class
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 */

public final class Configuration<K> {

	private final int branchingFactor;
	private final Comparator<? super K> keyComparator;
	
	public Configuration(int branchingFactor, Comparator<? super K> keyComparator) {
		if (branchingFactor < 2) {
			throw new IllegalArgumentException("illegal branchingFactor " + branchingFactor);
		}
		if (keyComparator == null) {
			throw new IllegalArgumentException("empty argument keyComparator");
		}
		this.branchingFactor = branchingFactor;
		this.keyComparator = keyComparator;
	}

	public static <K> Configuration<K> newInstance(int branchingFactor, Comparator<? super K> keyComparator) {
		return new Configuration<K>(branchingFactor, keyComparator);
	}
	
	public int getBranchingFactor() {
		return branchingFactor;
	}

	public Comparator<? super K> getKeyComparator() {
		return keyComparator;
	}

}
