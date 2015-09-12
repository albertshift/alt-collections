package alt.collections.tree;

/**
 * Supported entries in Trees
 * 
 * @author Albert Shift
 *
 */

public class Entries {

	public static <K, V> SimpleEntry<K, V> simple(K key, V value) {
		return new SimpleEntry<K, V>(key, value);
	}
	
}
