package alt.collections.tree.b;

import alt.collections.tree.Tree.Entry;

/**
 * Base Interface for all nodes
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public interface Node<K, V> {

	int getLength();
	
	Entry<K, V> get(Configuration<K> conf, K key);

	Object put(Configuration<K> conf, Entry<K, V> entry);

	Entry<K, V> remove(Configuration<K> conf, K key);

	void verify(Configuration<K> conf, boolean root);

	Entry<K, V> getFirstEntry();
	
	Entry<K, V> getNextEntry(Configuration<K> conf, K key);

	Entry<K, V> getLastEntry();

	Entry<K, V> removeFirst(Configuration<K> conf);
	
	Entry<K, V> removeLast(Configuration<K> conf);

	Entry<K, V> rotateClockwise(Configuration<K> conf, Entry<K, V> splitEntry, Node<K, V> greater);

	Entry<K, V> rotateCounterclockwise(Configuration<K> conf, Entry<K, V> splitEntry, Node<K, V> greater);
	
	void join(Entry<K, V> entry, Node<K, V> greater);
	
	int getChildPages();
	
	void print(String prefix); 
	
}