package alt.collections.tree.bplus;

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
	
	Object putLast(Configuration<K> conf, Entry<K, V> entry);
	
	Entry<K, V> updateLast(Entry<K, V> entry);

	Entry<K, V> remove(Configuration<K> conf, K key);

	void verify(Configuration<K> conf, boolean root);

	Entry<K, V> getFirstEntry();
	
	Entry<K, V> getNextEntry(Configuration<K> conf, K key);

	Entry<K, V> getLastEntry();

	Entry<K, V> removeFirst(Configuration<K> conf);
	
	Entry<K, V> removeLast(Configuration<K> conf);

	K rotateClockwise(Configuration<K> conf, K splitKey, Node<K, V> greater);

	K rotateCounterclockwise(Configuration<K> conf, K splitKey, Node<K, V> greater);
	
	void join(Node<K, V> greater);
	
	int getChildPages();
	
	void print(String prefix); 
	
}