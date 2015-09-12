package alt.collections.tree.bplus;

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
	
	private final Object[] keys;
	private final Node<K, V>[] childs;
	private int length;

	@SuppressWarnings("unchecked")
	private InnerNode(Configuration<K> conf) {
		this.keys = new Object[2 * conf.getBranchingFactor()];
		this.childs = (Node[]) Array.newInstance(Node.class, 2* conf.getBranchingFactor() + 1);
	}

	public InnerNode<K, V> splitFromLesserAt(Configuration<K> conf, int fromIndex) {
		InnerNode<K, V> greater = new InnerNode<K, V>(conf);
		greater.length = this.length - fromIndex;
		System.arraycopy(this.keys, fromIndex, greater.keys, 0, greater.length);
		System.arraycopy(this.childs, fromIndex, greater.childs, 0, greater.length+1);
		this.length = fromIndex;
		return greater;
	}
	
	public InnerNode<K, V> splitFromGreaterAt(Configuration<K> conf, int fromIndex, Node<K, V> firstChildForNewGreater) {
		InnerNode<K, V> greater = new InnerNode<K, V>(conf);
		greater.length = this.length - fromIndex;
		System.arraycopy(this.keys, fromIndex, greater.keys, 0, greater.length);
		System.arraycopy(this.childs, fromIndex+1, greater.childs, 1, greater.length);
		greater.childs[0] = firstChildForNewGreater;
		this.length = fromIndex;
		return greater;
	}
	
	public InnerNode(Configuration<K> conf, Node<K, V> lesser, K key, Node<K, V> greater) {
		this(conf);
		this.keys[0] = key;
		this.childs[0] = lesser;
		this.childs[1] = greater;
		this.length = 1;
	}
	
	@Override
	public int getLength() {
		return this.length;
	}

	@SuppressWarnings("unchecked")
	private int binarySearch(Object[] a, int fromIndex, int toIndex,
			K key, Comparator<? super K> keyComparator) {

		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low >>> 1) + (high >>> 1);
			mid += ((low & 1) + (high & 1)) >>> 1;
			
			//int mid = (low + high) >>> 1;
			Object midVal = a[mid];
			int cmp = keyComparator.compare((K) midVal, key);

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
	
	@SuppressWarnings("unchecked")
	protected int search(Configuration<K> conf, K key) {
		if (length > 7) {
			// JDK has a bug in int mid = (low + high) >>> 1; - int overflow
			return binarySearch(keys, 0, length, key, conf.getKeyComparator());
		}
		else {
		    for (int i = 0; i != length; i++) {
		    	int c = conf.getKeyComparator().compare((K)keys[i], key);
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
	
	@SuppressWarnings("unchecked")
	protected K pollFirstKey() {
		K key = (K) this.keys[0];
		deleteFromLesser(0);
		return key;
	}

	@SuppressWarnings("unchecked")
	protected K pollLastKey() {
		K key = (K) this.keys[this.length-1];
		this.length--;
		return key;			
	}

	protected void insert(int index, Node<K, V> child, K key) {
		int childIndex = index;
		int childLength = length+1;
		if (index < childLength) {
			System.arraycopy(childs, index, childs, childIndex+1, childLength-childIndex);
		}
		childs[childIndex] = child;
		
		if (index < length) {
			System.arraycopy(this.keys, index, this.keys, index+1, this.length-index);
		}
		this.keys[index] = key;
		this.length++;
	}
	
	protected void insert(int index, K key, Node<K, V> child) {
		int childIndex = index+1;
		int childLength = length+1;
		if (childIndex < childLength) {
			System.arraycopy(childs, childIndex, childs, childIndex+1, childLength-childIndex);
		}
		childs[childIndex] = child;
		
		if (index < length) {
			System.arraycopy(this.keys, index, this.keys, index+1, this.length-index);
		}
		this.keys[index] = key;
		this.length++;
	}
	
	@SuppressWarnings("unchecked")
	protected K replace(int index, K newKey) {
		K oldKey = (K) this.keys[index];
		this.keys[index] = newKey;
		return oldKey;
	}
	
	protected void deleteFromLesser(int index) {
		int childLast = length + 1 - 1;
		if (index < childLast) {
			System.arraycopy(childs, index+1, childs, index, childLast-index);
		}
		
		int last = this.length - 1;
		if (index < last) {
			System.arraycopy(this.keys, index + 1, this.keys, index, last-index);
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
			System.arraycopy(this.keys, index + 1, this.keys, index, last-index);
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
	public void join(Node<K, V> greater) {
		if (greater instanceof InnerNode) {
			InnerNode<K, V> greaterNode = (InnerNode<K, V>) greater;
			System.arraycopy(greaterNode.keys, 0, keys, this.length+1, greaterNode.length);
			this.keys[this.length] = this.getLastEntry().getKey();
			System.arraycopy(greaterNode.childs, 0, childs, this.length+1, greaterNode.length+1);
			this.length += 1 + greaterNode.length;
		}
		else {
			this.keys[this.length] = this.getLastEntry().getKey();
			this.childs[this.length+1] = greater;
			this.length++;
		}
	}
	
	@Override
	public Entry<K, V> get(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {
			return this.childs[index].getLastEntry();
		}
		index = -(index + 1);
		return this.childs[index].get(conf, key);
	}

	@Override
	public Object put(Configuration<K> conf, Entry<K, V> entry) {
		
		int index = search(conf, entry.getKey());
		if (index >= 0) {
			return this.childs[index].updateLast(entry);
		}
		
		index = -(index + 1);
		
		Object result = this.childs[index].put(conf, entry);
		
		if (result == null) {
			return null;
		}
		
		if (result instanceof Split) {
			@SuppressWarnings("unchecked")
			Split<K, V> split = (Split<K, V>) result;

			if (this.length < this.keys.length) {
				insert(index, split.getKey(), split.getGreater());
				return null;
			}
			
			return split(conf, index, split);
			
		}
		else {
			return result;
		}

	}
	
	@Override
	public Object putLast(Configuration<K> conf, Entry<K, V> entry) {
		
		Object result = getLastChild().putLast(conf, entry);
		if (result == null) {
			return null;
		}
		
		int index = this.length;
		
		if (result instanceof Split) {
			@SuppressWarnings("unchecked")
			Split<K, V> split = (Split<K, V>) result;
			
			if (this.length < this.keys.length) {
				insert(index, split.getKey(), split.getGreater());
				return null;
			}
			
			return split(conf, index, split);
		}
		else {
			return result;
		}
	}

	@Override
	public Entry<K, V> updateLast(Entry<K, V> entry) {
		return getLastChild().updateLast(entry);
	}
	
	@Override
	public Entry<K, V> remove(Configuration<K> conf, K key) {
		
		int index = search(conf, key);
		if (index >= 0) {		
			Entry<K, V> removedEntry = this.childs[index].removeLast(conf);
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
			return lesserChild.getLength() + greaterChild.getLength() <= this.keys.length;
		}
		else {
			return lesserChild.getLength() + greaterChild.getLength() + 1 <= this.keys.length;
		}
	}
	
	protected void removeByIndex(Configuration<K> conf, int index) {
		
		Node<K, V> lesserChild = this.childs[index];
		Node<K, V> greaterChild = this.childs[index+1];

		if (canJoin(lesserChild, greaterChild)) {
			
			lesserChild.join(greaterChild);
			deleteFromGreater(index);
		}
		
		else {
			
			Entry<K, V> entry = lesserChild.getLastEntry();
			this.keys[index] = entry.getKey();
			
			balance(conf, index);
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

		@SuppressWarnings("unchecked")
		K splitKey = (K) this.keys[index];
		
		if (lesserChild.getLength() + greaterChild.getLength() + 1 <= this.keys.length) {
							
			lesserChild.join(greaterChild);
			deleteFromGreater(index);
			
			return true;
		}

		if (lesserChild.getLength() < conf.getBranchingFactor() && greaterChild.getLength() > conf.getBranchingFactor()) {
			
			K key = lesserChild.rotateCounterclockwise(conf, splitKey, greaterChild);
			if (key != null) {

				this.keys[index] = key;
				return true;

			}
			
		}

		if (lesserChild.getLength() > conf.getBranchingFactor() && greaterChild.getLength() < conf.getBranchingFactor()) {

			K key = lesserChild.rotateClockwise(conf, splitKey, greaterChild);
			if (key != null) {
				
				this.keys[index] = key;
				return true;

			}
			
		}

		return false;
	}
	
	@Override
	public K rotateClockwise(Configuration<K> conf, K splitKey, Node<K, V> greater) {
		if (greater instanceof InnerNode) {
			
			InnerNode<K ,V> greaterInner = (InnerNode<K, V>) greater;
			
			Node<K ,V> child = this.getLastChild();
			K key = this.pollLastKey();
			greaterInner.insert(0, child, splitKey);
			
			return key;
			
		}
		return null;
	}

	@Override
	public K rotateCounterclockwise(Configuration<K> conf, K splitKey, Node<K, V> greater) {
		if (greater instanceof InnerNode) {
			
			InnerNode<K, V> greaterInner = (InnerNode<K, V>) greater;
			
			Node<K, V> child = greaterInner.getFirstChild();
			K key = greaterInner.pollFirstKey();
			this.insert(this.length, splitKey, child);
			
			return key;
			
		}
		return null;
	}

	protected Object split(Configuration<K> conf, int index, Split<K, V> split) {
		
		if (index == conf.getBranchingFactor()) {

			InnerNode<K, V> greater = this.splitFromGreaterAt(conf, conf.getBranchingFactor(), split.getGreater());
			return new Split<K, V>(split.getKey(), greater);
		}

		else if (index < conf.getBranchingFactor()) {

			InnerNode<K, V> greater = this.splitFromLesserAt(conf, conf.getBranchingFactor());
			K splitKey = this.pollLastKey();
			
			this.insert(index, split.getKey(), split.getGreater());

			return new Split<K, V>(splitKey, greater);
		}
		
		else {
			InnerNode<K, V> greater = this.splitFromLesserAt(conf, conf.getBranchingFactor() + 1);
			K splitKey = this.pollLastKey(); 
	
			greater.insert(index - conf.getBranchingFactor() - 1, split.getKey(), split.getGreater());
			
			return new Split<K, V>(splitKey, greater);
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
			System.out.println(prefix + "  " + this.keys[i]);
		}
		this.childs[length].print(prefix + "  ");
	}
	
	@SuppressWarnings("unchecked")
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
				prevKey = (K) this.keys[i1];
			}
			else {
				int c1 = conf.getKeyComparator().compare(prevKey, (K) this.keys[i1]);
				if (c1 != -1) {
					throw new IllegalStateException("Node " + this + " has not unordered keys at " + i1);
				}
			}
		}
		
		for (int i = 0; i != length; ++i) {
			Node<K, V> lesserChild = this.childs[i];
			Node<K, V> greaterChild = this.childs[i+1];
			
			K lesserKey = lesserChild.getLastEntry().getKey();
			K key = (K) this.keys[i];
			K greaterKey = greaterChild.getFirstEntry().getKey();
			
			int c = conf.getKeyComparator().compare(lesserKey, key);
			if (c > 0) {
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