package alt.collections.store;

/**
 * 
 * @author Albert Shift
 *
 */

public class Stores {

	public static FileStoreBuilder fileStore() {
		return new FileStoreBuilder();
	}
	
	public static FileStoreBuilder fileStore(int pageSize) {
		return new FileStoreBuilder().withPageSize(pageSize);
	}
	
	public static MemStoreBuilder memStore() {
		return new MemStoreBuilder();
	}

	public static MemStoreBuilder memStore(int pageSize) {
		return new MemStoreBuilder().withPageSize(pageSize);
	}

}
