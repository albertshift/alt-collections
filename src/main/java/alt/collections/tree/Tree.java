package alt.collections.tree;

/**
 * Base Tree interface for all trees
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public interface Tree<K, V> {

	<E extends Entry<K, V>> E get(K key);
	
	<E extends Entry<K, V>> E put(E entry);

	<E extends Entry<K, V>> E remove(K key);
	
	<E extends Entry<K, V>> E getFirstEntry();
	
	<E extends Entry<K, V>> E getNextEntry(K key);
	
	<E extends Entry<K, V>> E getLastEntry();
	
	<E extends Entry<K, V>> E removeFirst();

	<E extends Entry<K, V>> E removeLast();
	
	boolean isEmpty();
	
	int size();
	
	void clear();
	
	void verify();
	
	void print();
	
	<E extends Entry<K, V>>  Iterable<E> entries();
	
	/**
	 * Base Entry interface for all trees 
	 * 
	 * @author Albert Shift
	 *
	 * @param <K> Key type
	 * @param <V> Value type
	 */
	
	public interface Entry<K, V> {
		
		K getKey();
		
		V getValue();
		
	}
}
