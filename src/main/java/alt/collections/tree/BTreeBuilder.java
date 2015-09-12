package alt.collections.tree;

import java.util.Comparator;

import alt.collections.tree.b.BTree;
import alt.collections.tree.b2.B2Tree;
import alt.collections.tree.bplus.BPlusTree;
import alt.collections.util.Requires;

/**
 * Simple Heap B-Tree Builder
 * 
 * @author Albert Shift
 *
 */

public class BTreeBuilder {

	private enum Type {
		B_TREE, B2_TREE, BPLUS_TREE;
	}
	
	private int branchingFactor; 
	private Comparator<?> keyComparator;
	private Type type = Type.B_TREE;
	
	public BTreeBuilder withBranchingFactor(int branchingFactor) {
		this.branchingFactor = branchingFactor;
		return this;
	}
	
	public BTreeBuilder withKeyComparator(Comparator<?> keyComparator) {
		this.keyComparator = keyComparator;
		return this;
	}

	public BTreeBuilder useBTree() {
		this.type = Type.B_TREE;
		return this;
	}
	
	public BTreeBuilder useB2Tree() {
		this.type = Type.B2_TREE;
		return this;
	}
	
	public BTreeBuilder useBPlusTree() {
		this.type = Type.BPLUS_TREE;
		return this;
	}
	
	public <K, V> Tree<K, V> build() {
		Requires.nonZero(branchingFactor, "branchingFactor");
		Requires.nonNull(keyComparator, "keyComparator");
		switch (type) {
			case B_TREE:
				return BTree.<K, V>newInstance(branchingFactor, (Comparator<K>) keyComparator);
			case B2_TREE:
				return B2Tree.<K, V>newInstance(branchingFactor, (Comparator<K>) keyComparator);
			case BPLUS_TREE:
				return BPlusTree.<K, V>newInstance(branchingFactor, (Comparator<K>) keyComparator);
		}
		throw new IllegalStateException("unknown type " + type);
	}
	
}
