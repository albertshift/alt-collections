package alt.collections.tree;


/**
 * Simple entry POJO
 * 
 * @author Albert Shift
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public final class SimpleEntry<K, V> implements Tree.Entry<K, V> {

	private final K key;
	private final V value;

	public SimpleEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public static <K, V> SimpleEntry<K, V> newEntry(K key, V value) {
		return new SimpleEntry<K, V>(key, value);
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "SimpleEntry [key=" + key + ", value=" + value + "]";
	}
	
}