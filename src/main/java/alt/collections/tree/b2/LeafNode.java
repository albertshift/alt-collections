package alt.collections.tree.b2;

import java.lang.reflect.Array;
import java.util.Comparator;

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

public final class LeafNode<K, V> implements Node<K, V> {
	
	private final Entry<K, V>[] entries;
	private int length;
	
	@SuppressWarnings("unchecked")
	public LeafNode(Configuration<K> conf) {
		this.entries = (Entry[]) Array.newInstance(Entry.class, 2 * conf.getBranchingFactor());
	}
	
	public LeafNode<K, V> splitAt(Configuration<K> conf, int fromIndex) {
		LeafNode<K, V> greater = new LeafNode<K, V>(conf);
		greater.length = this.length - fromIndex;
		System.arraycopy(this.entries, fromIndex, greater.entries, 0, greater.length);
		this.length = fromIndex;
		return greater;
	}
	
	@Override
	public int getLength() {
		return this.length;
	}

	@Override
	public void join(Entry<K, V> entry, Node<K, V> greater) {
		if (greater instanceof LeafNode) {
			LeafNode<K, V> greaterNode = (LeafNode<K, V>) greater;
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

	protected Split<K, V> split(Configuration<K> conf, int index, Entry<K, V> newEntry) {
		
		
		if (index == conf.getBranchingFactor()) {
			
			LeafNode<K, V> greater = this.splitAt(conf, conf.getBranchingFactor());
			
			return new Split<K, V>(newEntry, greater);
		}
		
		else if (index < conf.getBranchingFactor()) {
			
			LeafNode<K, V> greater = this.splitAt(conf, conf.getBranchingFactor());
			Entry<K, V> splitEntry = pollLast();
			
			this.insert(index, newEntry);
			
			return new Split<K, V>(splitEntry, greater);
			
		}
	
		else {
			LeafNode<K, V> greater = this.splitAt(conf, conf.getBranchingFactor() + 1);
			Entry<K, V> splitEntry = this.pollLast();
			
			greater.insert(index - conf.getBranchingFactor() - 1, newEntry);
			
			return new Split<K, V>(splitEntry, greater);
		}
	}

	@Override
	public void print(String prefix) {
		System.out.println(prefix + "LeafNode " + FormatUtil.getHexAddress(this) + ", length=" + length);
		for (int i = 0; i != length; ++i) {
			System.out.println(prefix + "  " + this.entries[i]);
		}
	}
	
}