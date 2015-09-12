package alt.collections.tree.b2;

import java.lang.reflect.Array;
import java.util.Comparator;

import alt.collections.tree.Tree.Entry;
import alt.collections.util.FormatUtil;

/**
 * Inner Node implementation
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public final class InnerNode<K, V> implements Node<K, V> {
	
	private final Entry<K, V>[] entries;
	private final Node<K, V>[] childs;
	private int length;

	@SuppressWarnings("unchecked")
	private InnerNode(Configuration<K> conf) {
		this.entries = (Entry[]) Array.newInstance(Entry.class, 2 * conf.getBranchingFactor());
		this.childs = (Node[]) Array.newInstance(Node.class, 2* conf.getBranchingFactor() + 1);
	}

	public InnerNode<K, V> splitFromLesserAt(Configuration<K> conf, int fromIndex) {
		InnerNode<K, V> greater = new InnerNode<K, V>(conf);
		greater.length = this.length - fromIndex;
		System.arraycopy(this.entries, fromIndex, greater.entries, 0, greater.length);
		System.arraycopy(this.childs, fromIndex, greater.childs, 0, greater.length+1);
		this.length = fromIndex;
		return greater;
	}
	
	public InnerNode<K, V> splitFromGreaterAt(Configuration<K> conf, int fromIndex, Node<K, V> firstChildForNewGreater) {
		InnerNode<K, V> greater = new InnerNode<K, V>(conf);
		greater.length = this.length - fromIndex;
		System.arraycopy(this.entries, fromIndex, greater.entries, 0, greater.length);
		System.arraycopy(this.childs, fromIndex+1, greater.childs, 1, greater.length);
		greater.childs[0] = firstChildForNewGreater;
		this.length = fromIndex;
		return greater;
	}
	
	public InnerNode(Configuration<K> conf, Node<K, V> lesser, Entry<K, V> entry, Node<K, V> greater) {
		this(conf);
		this.entries[0] = entry;
		this.childs[0] = lesser;
		this.childs[1] = greater;
		this.length = 1;
	}
	
	@Override
	public int getLength() {
		return this.length;
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

		deleteFromLesser(0);
		return entry;
	}

	protected Entry<K, V> pollLast() {
		Entry<K, V> entry = this.entries[this.length-1];
		this.length--;
		return entry;			
	}

	protected void insert(int index, Node<K, V> child, Entry<K, V> entry) {
		int childIndex = index;
		int childLength = length+1;
		if (index < childLength) {
			System.arraycopy(childs, index, childs, childIndex+1, childLength-childIndex);
		}
		childs[childIndex] = child;
		
		if (index < length) {
			System.arraycopy(this.entries, index, this.entries, index+1, this.length-index);
		}
		this.entries[index] = entry;
		this.length++;
	}
	
	protected void insert(int index, Entry<K, V> entry, Node<K, V> child) {
		int childIndex = index+1;
		int childLength = length+1;
		if (childIndex < childLength) {
			System.arraycopy(childs, childIndex, childs, childIndex+1, childLength-childIndex);
		}
		childs[childIndex] = child;
		
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
	
	protected void deleteFromLesser(int index) {
		int childLast = length + 1 - 1;
		if (index < childLast) {
			System.arraycopy(childs, index+1, childs, index, childLast-index);
		}
		
		int last = this.length - 1;
		if (index < last) {
			System.arraycopy(this.entries, index + 1, this.entries, index, last-index);
		}
		this.length--;
	}
	
	protected void deleteFromGreater(int index) {
		int childIndex = index+1;
		int childLast = length + 1 - 1;
		if (childIndex < childLast) {
			System.arraycopy(childs, childIndex+1, childs, childIndex, childLast-childIndex);
		}
		
		int last = this.length - 1;
		if (index < last) {
			System.arraycopy(this.entries, index + 1, this.entries, index, last-index);
		}
		this.length--;
	}
	
	@Override
	public int getChildPages() {
		
		int count = 0;
		
		for (int i = 0; i != length+1; ++i) {
			count += 1 + childs[i].getChildPages();
			
		}
		
		return count;
	}
	
	@Override
	public void join(Entry<K, V> entry, Node<K, V> greater) {
		if (greater instanceof InnerNode) {
			InnerNode<K, V> greaterNode = (InnerNode<K, V>) greater;
			System.arraycopy(greaterNode.entries, 0, entries, this.length+1, greaterNode.length);
			this.entries[this.length] = entry;
			System.arraycopy(greaterNode.childs, 0, childs, this.length+1, greaterNode.length+1);
			this.length += 1 + greaterNode.length;
		}
		else {
			this.entries[this.length] = entry;
			this.childs[this.length+1] = greater;
			this.length++;
		}
	}
	
	@Override
	public Entry<K, V> get(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {
			return this.entries[index];
		}
		index = -(index + 1);
		return this.childs[index].get(conf, key);
	}

	@Override
	public Object put(Configuration<K> conf, Entry<K, V> entry) {
		
		int index = search(conf, entry.getKey());
		if (index >= 0) {
			return replace(index, entry);
		}
		
		index = -(index + 1);
		
		Object result = this.childs[index].put(conf, entry);
		
		if (result == null) {
			return null;
		}
		
		if (result instanceof Split) {
			@SuppressWarnings("unchecked")
			Split<K, V> split = (Split<K, V>) result;

			if (this.length < this.entries.length) {
				insert(index, split.getEntry(), split.getGreater());
				return null;
			}
			
			return split(conf, index, split);
			
		}
		else {
			return result;
		}

	}
	
	@Override
	public Entry<K, V> remove(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {		
			Entry<K, V> removedEntry = this.entries[index];
			removeByIndex(conf, index);
			return removedEntry;
		}
		
		index = -(index + 1);
		
		Entry<K, V> removedEntry = this.childs[index].remove(conf, key);
		
		if (removedEntry != null) {
			balance(conf, index);
		}
		return removedEntry;
	}
	
	protected boolean canJoin(Node<K, V> lesserChild, Node<K, V> greaterChild) {
		if (lesserChild instanceof LeafNode) {
			return lesserChild.getLength() + greaterChild.getLength() <= this.entries.length;
		}
		else {
			return lesserChild.getLength() + greaterChild.getLength() + 1 <= this.entries.length;
		}
	}
	
	protected void removeByIndex(Configuration<K> conf, int index) {
		
		Node<K, V> lesserChild = this.childs[index];
		Node<K, V> greaterChild = this.childs[index+1];

		if (canJoin(lesserChild, greaterChild)) {
			
			Entry<K, V> entry = lesserChild.removeLast(conf);
			lesserChild.join(entry, greaterChild);

			deleteFromGreater(index);
			
			return;
		}
		
		if (lesserChild.getLength() >= greaterChild.getLength()) {
			
			Entry<K, V> entry = lesserChild.removeLast(conf);
			this.entries[index] = entry;
			
			balance(conf, index);
			return;
		}
		else {
			
			Entry<K, V> entry = greaterChild.removeFirst(conf);
			this.entries[index] = entry;
			
			balance(conf, index);
			return;
		}

	}
	
	protected void balance(Configuration<K> conf, int index) {
		if (index == length) {
			joinOrRotate(conf, index-1);
		}
		else if (index == 0) {
			joinOrRotate(conf, 0);
		}
		else if (!joinOrRotate(conf, index-1)) {
			joinOrRotate(conf, index);
		}
	}
	
	protected boolean joinOrRotate(Configuration<K> conf, int index) {

		Node<K, V> lesserChild = this.childs[index];
		Node<K, V> greaterChild = this.childs[index+1];

		Entry<K, V> splitEntry = this.entries[index];
		
		if (lesserChild.getLength() + greaterChild.getLength() + 1 <= this.entries.length) {
							
			lesserChild.join(splitEntry, greaterChild);
			deleteFromGreater(index);
			
			return true;
		}

		if (lesserChild.getLength() < conf.getBranchingFactor() && greaterChild.getLength() > conf.getBranchingFactor()) {
			
			Entry<K, V> entry = lesserChild.rotateCounterclockwise(conf, splitEntry, greaterChild);
			if (entry != null) {

				this.entries[index] = entry;
				return true;

			}
			
		}

		if (lesserChild.getLength() > conf.getBranchingFactor() && greaterChild.getLength() < conf.getBranchingFactor()) {

			Entry<K, V> entry = lesserChild.rotateClockwise(conf, splitEntry, greaterChild);
			if (entry != null) {
				
				this.entries[index] = entry;
				return true;

			}
			
		}

		return false;
	}
	
	@Override
	public Entry<K, V> rotateClockwise(Configuration<K> conf, Entry<K, V> splitEntry, Node<K, V> greater) {
		if (greater instanceof InnerNode) {
			
			InnerNode<K ,V> greaterInner = (InnerNode<K, V>) greater;
			
			Node<K ,V> child = this.getLastChild();
			Entry<K, V> entry = this.pollLast();
			greaterInner.insert(0, child, splitEntry);
			
			return entry;
			
		}
		return null;
	}

	@Override
	public Entry<K, V> rotateCounterclockwise(Configuration<K> conf, Entry<K, V> splitEntry, Node<K, V> greater) {
		if (greater instanceof InnerNode) {
			
			InnerNode<K, V> greaterInner = (InnerNode<K, V>) greater;
			
			Node<K, V> child = greaterInner.getFirstChild();
			Entry<K, V> entry = greaterInner.pollFirst();
			this.insert(this.length, splitEntry, child);
			
			return entry;
			
		}
		return null;
	}

	protected Object split(Configuration<K> conf, int index, Split<K, V> split) {
		
		if (index == conf.getBranchingFactor()) {

			InnerNode<K, V> greater = this.splitFromGreaterAt(conf, conf.getBranchingFactor(), split.getGreater());
			
			return new Split<K, V>(split.getEntry(), greater);
		}

		else if (index < conf.getBranchingFactor()) {

			InnerNode<K, V> greater = this.splitFromLesserAt(conf, conf.getBranchingFactor());
			Entry<K, V> splitEntry = pollLast();
			
			this.insert(index, split.getEntry(), split.getGreater());

			return new Split<K, V>(splitEntry, greater);
		}
		
		else {
		
			InnerNode<K, V> greater = this.splitFromLesserAt(conf, conf.getBranchingFactor() + 1);
			Entry<K, V> splitEntry = this.pollLast(); 
	
			greater.insert(index - conf.getBranchingFactor() - 1, split.getEntry(), split.getGreater());
			
			return new Split<K, V>(splitEntry, greater);
		}
	}

	@Override
	public Entry<K, V> getFirstEntry() {
		return this.childs[0].getFirstEntry();
	}

	@Override
	public Entry<K, V> getNextEntry(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {
			return this.childs[index+1].getFirstEntry();
		}
		
		index = -(index + 1);
		
		Entry<K, V> nextEntry = this.childs[index].getNextEntry(conf, key);
		
		if (nextEntry == null && index < this.length) {
			nextEntry = this.entries[index];
		}
		
		return nextEntry;
	}
	
	@Override
	public Entry<K, V> getLastEntry() {
		return this.childs[length].getLastEntry();
	}
	
	@Override
	public Entry<K, V> removeFirst(Configuration<K> conf) {
		Entry<K, V> entry = this.childs[0].removeFirst(conf);
		balance(conf, 0);
		return entry;
	}

	@Override
	public Entry<K, V> removeLast(Configuration<K> conf) {
		Entry<K, V> entry = this.childs[length].removeLast(conf);
		balance(conf, length);
		return entry;
	}

	public Node<K, V> getFirstChild() {
		return this.childs[0];
	}

	public Node<K, V> getLastChild() {
		return this.childs[length];
	}
	
	@Override
	public void print(String prefix) {
		System.out.println(prefix + "InnerNode " + FormatUtil.getHexAddress(this) + ", length=" + this.length);
		for (int i = 0; i != length; ++i) {
			this.childs[i].print(prefix + "  ");
			System.out.println(prefix + "  " + this.entries[i]);
		}
		this.childs[length].print(prefix + "  ");
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
		for (int i1 = 0; i1 != length; ++i1) {
			if (prevKey == null) {
				prevKey = this.entries[i1].getKey();
			}
			else {
				int c1 = conf.getKeyComparator().compare(prevKey, this.entries[i1].getKey());
				if (c1 != -1) {
					throw new IllegalStateException("Node " + this + " has not unordered keys at " + i1);
				}
			}
		}
		
		for (int i = 0; i != length; ++i) {
			Node<K, V> lesserChild = this.childs[i];
			Node<K, V> greaterChild = this.childs[i+1];
			
			K lesserKey = lesserChild.getLastEntry().getKey();
			K key = this.entries[i].getKey();
			K greaterKey = greaterChild.getFirstEntry().getKey();
			
			int c = conf.getKeyComparator().compare(lesserKey, key);
			if (c != -1) {
				throw new IllegalStateException("invalid key in lesserChild " + lesserKey + ", current key = "+ key + ", Node = " + this);
			}
			
			c = conf.getKeyComparator().compare(key, greaterKey);
			if (c != -1) {
				throw new IllegalStateException("invalid key in greaterChild " + greaterKey + ", current key = "+ key + ", Node = " + this);
			}
		}
		
		for (int i = 0; i != length + 1; ++i) {
			this.childs[i].verify(conf, false);
		}
	}
	
}