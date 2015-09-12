package alt.collections.tree.bplus;


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
	
	private final K key;
	private final Node<K, V> greater;
	
	public Split(K key, Node<K, V> greater) {
		this.key = key;
		this.greater = greater;
	}

	public K getKey() {
		return key;
	}

	public Node<K, V> getGreater() {
		return greater;
	}

}