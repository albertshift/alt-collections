package alt.collections.tree.b2;

import alt.collections.tree.Tree.Entry;

/**
 * Split object
 * 
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public final class Split<K, V> {
	
	private final Entry<K, V> entry;
	private final Node<K, V> greater;
	
	public Split(Entry<K, V> entry, Node<K, V> greater) {
		this.entry = entry;
		this.greater = greater;
	}

	public Entry<K, V> getEntry() {
		return entry;
	}

	public Node<K, V> getGreater() {
		return greater;
	}

}