package alt.collections.tree.b;

import alt.collections.tree.Tree.Entry;
import alt.collections.util.FormatUtil;

/**
 * Leaf Node implementation
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public final class LeafNode<K, V> extends AbstractNode<K, V> {
	
	public LeafNode(Configuration<K> conf) {
		super(conf);
	}

	public LeafNode(Configuration<K> conf, AbstractNode<K, V> src, int fromIndex) {
		super(conf, src, fromIndex);
	}
	
	@Override
	public int getChildPages() {
		return 0;
	}

	@Override
	public Entry<K, V> get(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {
			return this.entries[index];
		}
		return null;
	}
	
	@Override
	public Object put(Configuration<K> conf, Entry<K, V> entry) {
		
		if (length == 0) {
			insert(0, entry);
			return null;
		}
		
		int index = search(conf, entry.getKey());
		if (index >= 0) {
			return replace(index, entry);
		}
		
		index = -(index + 1);
		
		if (this.length < this.entries.length) {
			insert(index, entry);
			return null;
		}
		
		return split(conf, index, entry);
	}

	@Override
	public Entry<K, V> remove(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {
			Entry<K, V> removedEntry = this.entries[index];
			delete(index);
			return removedEntry;
		}
		
		return null;
	}
	
	@Override
	public Entry<K, V> getFirstEntry() {
		return this.entries[0];
	}

	@Override
	public Entry<K, V> getNextEntry(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {
			return index+1 < this.length ? this.entries[index+1] : null;
		}
		
		index = -(index + 1);
		
		return index < this.length ? this.entries[index] : null;
		
	}
	
	@Override
	public Entry<K, V> getLastEntry() {
		return this.entries[this.length-1];
	}

	@Override
	public Entry<K, V> removeFirst(Configuration<K> conf) {
		return pollFirst();
	}

	@Override
	public Entry<K, V> removeLast(Configuration<K> conf) {
		return pollLast();
	}

	@Override
	public Entry<K, V> rotateClockwise(Configuration<K> conf, Entry<K, V> splitEntry, Node<K, V> greater) {
		if (greater instanceof LeafNode) {
			
			LeafNode<K, V> greaterLeaf = (LeafNode<K, V>) greater;
			
			Entry<K, V> entry = this.removeLast(conf);
			greaterLeaf.insert(0, splitEntry);
			
			return entry;
		}
		return null;
	}

	@Override
	public Entry<K, V> rotateCounterclockwise(Configuration<K> conf, Entry<K, V> splitEntry, Node<K, V> greater) {
		if (greater instanceof LeafNode) {
			
			LeafNode<K, V> greaterLeaf = (LeafNode<K, V>) greater;
			
			Entry<K, V> entry = greaterLeaf.removeFirst(conf);
			this.insert(this.length, splitEntry);
			
			return entry;
		}
		return null;
	}

	protected Split<K, V> split(Configuration<K> conf, int index, Entry<K, V> splitEntry) {
		
		if (index <= conf.getBranchingFactor()) {
			
			LeafNode<K, V> greater = new LeafNode<K, V>(conf, this, conf.getBranchingFactor());
			this.length = conf.getBranchingFactor();
			
			if (index == conf.getBranchingFactor()) {
				return new Split<K, V>(splitEntry, greater);
			}
			else {
				Entry<K, V> entry = pollLast();
				this.insert(index, splitEntry);
				return new Split<K, V>(entry, greater);
			}
			
		}
			
		Entry<K, V> entry = this.entries[conf.getBranchingFactor()];

		LeafNode<K, V> greater = new LeafNode<K, V>(conf, this, conf.getBranchingFactor() + 1);
		this.length = conf.getBranchingFactor();
		
		greater.insert(index - conf.getBranchingFactor() - 1, splitEntry);
		
		return new Split<K, V>(entry, greater);
	}

	@Override
	public void print(String prefix) {
		System.out.println(prefix + "LeafNode " + FormatUtil.getHexAddress(this) + ", length=" + length);
		for (int i = 0; i != length; ++i) {
			System.out.println(prefix + "  " + this.entries[i]);
		}
	}
	
}