package alt.collections.tree.b2;

import java.util.Comparator;
import java.util.Iterator;

import alt.collections.tree.Tree;

/**
 * Simple implementation of the B-Tree
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public final class B2Tree<K, V> implements Tree<K, V> {

	private final Configuration<K> conf;
	
	private Node<K, V> root;
	private int size;
	private Entry<K, V> nullEntry;

	public B2Tree(Configuration<K> conf) {
		this.conf = conf;
		this.root = new LeafNode<K, V>(conf);
	}
	
	public static <K, V> B2Tree<K, V> newInstance(int branchingFactor, final Comparator<? super K> keyComparator) {
		return new B2Tree<K, V>(Configuration.newInstance(branchingFactor, keyComparator));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<K, V>> E get(K key) {
		if (key == null) {
			return (E) this.nullEntry;
		}
		
		return (E) this.root.get(conf, key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<K, V>> E put(E entry) {
		if (entry.getKey() == null) {
			return updateNullEntry(entry);
		}
		
		if (entry.getValue() == null) {
			return remove(entry.getKey());
		}
		
		Object result = root.put(conf, entry);
		if (result == null) {
			this.size++;
			return null;
		}
		if (result instanceof Split) {
			Split<K, V> split = (Split<K, V>) result;
			this.root = new InnerNode<K, V>(conf, root, split.getEntry(), split.getGreater());
			this.size++;
			return null;
		}
		else {
			return (E) result;
		}
	}

	public <E extends Entry<K, V>>  E remove(K key) {
		if (key == null) {
			return updateNullEntry(null);
		}
		
		@SuppressWarnings("unchecked")
		E removedEntry = (E) root.remove(conf, key);
		if (removedEntry != null) {
			afterRemoveProcessing();
		}
		return removedEntry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<K, V>>  E getFirstEntry() {
		if (hasNullEntry()) {
			return (E) this.nullEntry;
		}
		if (hasTreeEntries()) {
			return (E) root.getFirstEntry();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<K, V>>  E getNextEntry(K key) {
		if (key == null) {
			return (E) root.getFirstEntry();
		}
		return (E) root.getNextEntry(conf, key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<K, V>>  E getLastEntry() {
		if (hasTreeEntries()) {
			return (E) root.getLastEntry();
		}
		if (hasNullEntry()) {
			return (E) this.nullEntry;
		}
		return null;
	}
	
	@Override
	public <E extends Entry<K, V>>  E removeFirst() {
		if (isEmpty()) {
			return null;
		}
		if (hasNullEntry()) {
			return updateNullEntry(null);
		}
		@SuppressWarnings("unchecked")
		E removedEntry = (E) root.removeFirst(conf);
		if (removedEntry != null) {
			afterRemoveProcessing();
		}
		return removedEntry;
	}

	@Override
	public <E extends Entry<K, V>>  E removeLast() {
		if (isEmpty()) {
			return null;
		}
		if (hasTreeEntries()) {
			@SuppressWarnings("unchecked")
			E removedEntry = (E) root.removeLast(conf);
			if (removedEntry != null) {
				afterRemoveProcessing();
			}
			return removedEntry;
		}
		return updateNullEntry(null);
	}

	private void afterRemoveProcessing() {
		if (root instanceof InnerNode && root.getLength() == 0) {
			InnerNode<K, V> inner = (InnerNode<K, V>) root;
			this.root = inner.getFirstChild();
		}
		this.size--;
	}
	
	public void print() {
		root.print("");
	}

	@Override
	public boolean isEmpty() {
		return !hasNullEntry() && !hasTreeEntries();
	}
	
	private boolean hasTreeEntries() {
		if (root instanceof LeafNode && root.getLength() == 0) {
			return false;
		}
		return true;
	}
	
	private boolean hasNullEntry() {
		return this.nullEntry != null;
	}
	
	private <E extends Entry<K, V>>  E updateNullEntry(E entry) {
		@SuppressWarnings("unchecked")
		E result = (E) this.nullEntry;
		this.nullEntry = entry;
		return result;
	}
	
	@Override
	public int size() {
		return this.size + (hasNullEntry() ? 1 : 0);
	}

	@Override
	public void clear() {
		this.root = new LeafNode<K, V>(conf);
		this.size = 0;
	}
	
	@Override
	public void verify() {
		this.root.verify(conf, true);
	}

	@Override
	public  <E extends Entry<K, V>> Iterable<E> entries() {
		return new IterableEntries<E>();
	}
	
	public class IterableEntries<E extends Entry<K, V>>  implements Iterable<E> {

		private final E firstEntry;
		
		public IterableEntries() {
			this.firstEntry = getFirstEntry();
		}
		
		@Override
		public Iterator<E> iterator() {
			return new Iterator<E>() {

				private E nextEntry = firstEntry;
				
				@Override
				public boolean hasNext() {
					return nextEntry != null;
				}

				@Override
				public E next() {
					E result = this.nextEntry;
					this.nextEntry = getNextEntry(this.nextEntry.getKey());
					return result;
				}

				@Override
				public void remove() {
					throw new IllegalStateException("unsupported operation");
				}
				
			};
		}
		
	}
	
	public int getTotalPages() {
		return 1 + this.root.getChildPages();
	}
	
}
