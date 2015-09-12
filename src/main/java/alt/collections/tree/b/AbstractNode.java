package alt.collections.tree.b;

import java.lang.reflect.Array;
import java.util.Comparator;

import alt.collections.tree.Tree.Entry;

/**
 * Abstract interface for common parts of Inner and Leaf Nodes in B-Tree
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public abstract class AbstractNode<K, V> implements Node<K, V> {
	
	protected final Entry<K, V>[] entries;
	protected int length;
	
	@SuppressWarnings("unchecked")
	public AbstractNode(Configuration<K> conf) {
		entries = (Entry[]) Array.newInstance(Entry.class, 2 * conf.getBranchingFactor());
	}
	
	public AbstractNode(Configuration<K> conf, AbstractNode<K, V> src, int fromIndex) {
		this(conf);
		this.length = src.length - fromIndex;
		System.arraycopy(src.entries, fromIndex, entries, 0, this.length);
	}
	
	@Override
	public int getLength() {
		return this.length;
	}

	public void join(Entry<K, V> entry, Node<K, V> greater) {
		if (greater instanceof AbstractNode) {
			AbstractNode<K, V> greaterNode = (AbstractNode<K, V>) greater;
			this.entries[this.length] = entry;
			System.arraycopy(greaterNode.entries, 0, entries, this.length+1, greaterNode.length);
			this.length += 1 + greaterNode.length;
		}
		else {
			throw new IllegalStateException("unknown Node class " + greater.getClass());
		}
	}
	
	private int binarySearch(Entry<K, V>[] a, int fromIndex, int toIndex,
			K key, Comparator<? super K> keyComparator) {

		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low >>> 1) + (high >>> 1);
			mid += ((low & 1) + (high & 1)) >>> 1;
			
			//int mid = (low + high) >>> 1;
			Entry<K, V> midVal = a[mid];
			int cmp = keyComparator.compare(midVal.getKey(), key);

			if (cmp < 0) {
				low = mid + 1;
			}
			else if (cmp > 0) {
				high = mid - 1;
			}
			else {
				return mid; // key found
			}
		}
		return -(low + 1); // key not found.
	}
	
	protected int search(Configuration<K> conf, K key) {
		if (length > 7) {
			// JDK has a bug in int mid = (low + high) >>> 1; - int overflow
			//return Arrays.binarySearch(entries, 0, length, new SearchE(key), entryComparator);
			return binarySearch(entries, 0, length, key, conf.getKeyComparator());
		}
		else {
		    for (int i = 0; i != length; i++) {
		    	int c = conf.getKeyComparator().compare(entries[i].getKey(), key);
				if (c == 0) {
				    return i;
				}
				else if (c > 0) {
					return -(i + 1);
				}
		    }
		    return -(length + 1);
		}
	}
	
	protected Entry<K, V> pollFirst() {
		Entry<K, V> entry = this.entries[0];
		delete(0);
		return entry;
	}

	protected Entry<K, V> pollLast() {
		Entry<K, V> entry = this.entries[this.length-1];
		this.length--;
		return entry;			
	}

	protected void insert(int index, Entry<K, V> entry) {
		if (index < length) {
			System.arraycopy(this.entries, index, this.entries, index+1, this.length-index);
		}
		this.entries[index] = entry;
		this.length++;
	}
	
	protected Entry<K, V> replace(int index, Entry<K, V> newEntry) {
		Entry<K, V> oldEntry = this.entries[index];
		this.entries[index] = newEntry;
		return oldEntry;
	}
	
	protected void delete(int index) {
		int last = this.length - 1;
		if (index < last) {
			System.arraycopy(this.entries, index + 1, this.entries, index, last-index);
		}
		this.length--;
	}
	
	@Override
	public void verify(Configuration<K> conf, boolean root) {
		if (length > conf.getBranchingFactor() * 2) {
			throw new IllegalStateException("too big length " + length + " in Node " + this);
		}
		if (!root) {
			if (length < conf.getBranchingFactor()) {
				throw new IllegalStateException("too low length " + length + " in Node " + this);
			}
		}
		
		K prevKey = null;
		for (int i = 0; i != length; ++i) {
			if (prevKey == null) {
				prevKey = this.entries[i].getKey();
			}
			else {
				int c = conf.getKeyComparator().compare(prevKey, this.entries[i].getKey());
				if (c != -1) {
					throw new IllegalStateException("Node " + this + " has not unordered keys at " + i);
				}
			}
		}
	}

}