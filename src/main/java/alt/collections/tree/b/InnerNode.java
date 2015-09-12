package alt.collections.tree.b;

import java.lang.reflect.Array;

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

public final class InnerNode<K, V> extends AbstractNode<K, V> {
	
	private final Node<K, V>[] childs;
	
	@SuppressWarnings("unchecked")
	public InnerNode(Configuration<K> conf, Node<K, V> firstChild, InnerNode<K, V> src, int fromIndex) {
		super(conf, src, fromIndex);
		this.childs = (Node[]) Array.newInstance(Node.class, 2* conf.getBranchingFactor() + 1);
		System.arraycopy(src.childs, fromIndex+1, childs, 1, this.length);
		childs[0] = firstChild;
	}

	@SuppressWarnings("unchecked")
	public InnerNode(Configuration<K> conf, InnerNode<K, V> src, int fromIndex) {
		super(conf, src, fromIndex);
		this.childs = (Node[]) Array.newInstance(Node.class, 2* conf.getBranchingFactor() + 1);
		System.arraycopy(src.childs, fromIndex, childs, 0, this.length+1);
	}

	@SuppressWarnings("unchecked")
	public InnerNode(Configuration<K> conf, Node<K, V> lesser, Entry<K, V> entry, Node<K, V> greater) {
		super(conf);
		this.childs = (Node[]) Array.newInstance(Node.class, 2* conf.getBranchingFactor() + 1);
		this.entries[0] = entry;
		this.childs[0] = lesser;
		this.childs[1] = greater;
		this.length = 1;
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
			InnerNode<K, V> inner = (InnerNode<K, V>) greater;
			System.arraycopy(inner.childs, 0, childs, this.length+1, greater.getLength()+1);
			super.join(entry, greater);
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
				insertChild(index+1, split.getGreater());
				insert(index, split.getEntry());
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

			deleteChild(index+1);
			delete(index);
			
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
			deleteChild(index+1);
			delete(index);
			
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
			
			Node<K ,V> child = this.pollLastChild();
			Entry<K, V> entry = this.pollLast();
			greaterInner.insertChild(0, child);
			greaterInner.insert(0, splitEntry);
			
			return entry;
			
		}
		return null;
	}

	@Override
	public Entry<K, V> rotateCounterclockwise(Configuration<K> conf, Entry<K, V> splitEntry, Node<K, V> greater) {
		if (greater instanceof InnerNode) {
			
			InnerNode<K, V> greaterInner = (InnerNode<K, V>) greater;
			
			Node<K, V> child = greaterInner.pollFirstChild();
			Entry<K, V> entry = greaterInner.pollFirst();
			this.insertChild(this.length+1, child);
			this.insert(this.length, splitEntry);
			
			return entry;
			
		}
		return null;
	}

	protected Object split(Configuration<K> conf, int index, Split<K, V> split) {
		
		if (index == conf.getBranchingFactor()) {

			InnerNode<K, V> greater = new InnerNode<K, V>(conf, split.getGreater(), this, conf.getBranchingFactor());
			this.length = conf.getBranchingFactor();
			return new Split<K, V>(split.getEntry(), greater);
		}

		if (index < conf.getBranchingFactor()) {

			InnerNode<K, V> greater = new InnerNode<K, V>(conf, this, conf.getBranchingFactor());
			this.length = conf.getBranchingFactor();
			Entry<K, V> splitEntry = pollLast();
			this.insertChild(index+1, split.getGreater());
			this.insert(index, split.getEntry());

			return new Split<K, V>(splitEntry, greater);
		}
		
		Entry<K, V> splitEntry = this.entries[conf.getBranchingFactor()];
		
		InnerNode<K, V> greater = new InnerNode<K, V>(conf, this, conf.getBranchingFactor() + 1);
		this.length = conf.getBranchingFactor();

		greater.insertChild(index - conf.getBranchingFactor(), split.getGreater());
		greater.insert(index - conf.getBranchingFactor() - 1, split.getEntry());
		
		return new Split<K, V>(splitEntry, greater);
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

	public Node<K, V> pollFirstChild() {
		Node<K, V> child = this.childs[0];
		deleteChild(0);
		return child;
	}

	public Node<K, V> pollLastChild() {
		return this.childs[length];
	}
	
	protected void insertChild(int childIndex, Node<K, V> child) {
		
		int childLength = length+1;
		if (childIndex < childLength) {
			System.arraycopy(childs, childIndex, childs, childIndex+1, childLength-childIndex);
		}
		childs[childIndex] = child;
		
	}

	protected void deleteChild(int index) {
		int last = length + 1 - 1;
		if (index < last) {
			System.arraycopy(childs, index+1, childs, index, last-index);
		}
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
		super.verify(conf, root);
		
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