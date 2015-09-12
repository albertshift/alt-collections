package alt.collections.tree.paging;

import java.io.PrintStream;

import alt.collections.concurrent.PageNumCas;
import alt.collections.paging.PageReader;
import alt.collections.paging.Paging;
import alt.collections.util.Requires;
import alt.collections.util.ThreadUtil;

/**
 * Paging Tree implementation
 * 
 * @author Albert Shift
 *
 */

public final class PagingTree<K, V> {

	private final Paging paging;
	private final String treeName;
	private final MasterPage masterPage;
	private final NamedTreeMap namedTreeMap;
	private final PageNumCas treeAddress;
	private final LeafNode leafNode;
	
	public PagingTree(Paging paging, String treeName) {
		this.paging = paging;
		this.treeName = treeName;
		this.masterPage = MasterPage.concurrentGetOrCreate(paging);  

		PagedVirtualSpace pagedVirtualSpace = new PagedVirtualSpace(paging, masterPage, masterPage);
		this.namedTreeMap = new NamedTreeImmutableMap(paging, pagedVirtualSpace, masterPage.getRootEntry());
		
		this.treeAddress = namedTreeMap.findOrCreate(treeName);
		
		this.leafNode = new LeafNode(paging);
	}

	public String getTreeName() {
		return treeName;
	}

	@SuppressWarnings("unchecked")
	public V get(K key) {
		Requires.nonNull(key, "key");
		
		return (V) doGet(key);
	}
	
	public boolean replace(K key, V oldValue, V newValue) {
		Requires.nonNull(key, "key");
		Requires.nonNull(oldValue, "oldValue");
		Requires.nonNull(newValue, "newValue");
		
		return doReplace(key, oldValue, newValue);
	}
	
	@SuppressWarnings("unchecked")
	public V putIfAbsent(K key, V value) {
		Requires.nonNull(key, "key");
		Requires.nonNull(value, "value");
		
		return (V) doPut(key, value, PUT_IF_ABSENT);
	 }

	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		Requires.nonNull(key, "key");
		Requires.nonNull(value, "value");

		return (V) doPut(key, value, PUT);
	}
	
	@SuppressWarnings("unchecked")
	public V replace(K key, V value) {
		Requires.nonNull(key, "key");
		Requires.nonNull(value, "value");

		return (V) doPut(key, value, REPLACE);
	}
	
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		Requires.nonNull(key, "key");

		return (V) doRemove(key);
	}
	
	public boolean remove(Object key, Object oldValue) {
		Requires.nonNull(key, "key");
		Requires.nonNull(oldValue, "oldValue");

		return doRemove(key, oldValue);
	}
	
	private Object doGet(Object key) {
		
		long pageNum = treeAddress.getPageNum();
		if (pageNum == 0) {
			return null;
		}
		
		PageReader pageReader = new PageReader(paging, pageNum);
		
		char magic = pageReader.readChar();
		
		if (magic == MagicCodes.INNER_NODE.getMagic()) {
			throw new IllegalStateException("unsupported INNER_NODE");
		}
		else if (magic == MagicCodes.LEAF_NODE.getMagic())  {
			return leafNode.get(pageReader, key);
		}
		else {
			throw new PagingTreeException("unknown magic " + Integer.toHexString(magic) + " for page " + pageNum);
		}
		
	}

	private boolean doReplace(Object key, Object oldValue, Object newValue) {
		
		long pageNum = treeAddress.getPageNum();
		if (pageNum == 0) {
			return false;
		}
		
		PageReader pageReader = new PageReader(paging, pageNum);
		
		char magic = pageReader.readChar();
		
		if (magic == MagicCodes.INNER_NODE.getMagic()) {
			throw new IllegalStateException("unsupported INNER_NODE");
		}
		else if (magic == MagicCodes.LEAF_NODE.getMagic())  {
			return leafNode.replace(pageReader, key, oldValue, newValue);
		}
		else {
			throw new PagingTreeException("unknown magic " + Integer.toHexString(magic) + " for page " + pageNum);
		}
		
	}
	
	private Object doPut(Object key, Object value, ValuePredicate valuePredicate) {
		
		long pageNum = treeAddress.getPageNum();
		if (pageNum == 0) {
			
			if (!valuePredicate.apply(false)) {
				return null;
			}
			
			// create new leaf page
			long newPageNum = masterPage.allocatePage();
			LeafNodePage.structBlank(paging, newPageNum, key, value);

			if (treeAddress.casPageNum(0, newPageNum)) {
				return null;
			}
			
			masterPage.freePage(newPageNum, false);
			ThreadUtil.loopSleep();
			return doPut(key, value, valuePredicate);
		}
		
		PageReader pageReader = new PageReader(paging, pageNum);
		
		char magic = pageReader.readChar();
		
		if (magic == MagicCodes.INNER_NODE.getMagic()) {
			throw new IllegalStateException("unsupported INNER_NODE");
		}
		else if (magic == MagicCodes.LEAF_NODE.getMagic())  {
			return leafNode.put(pageReader, key, value, valuePredicate);
		}
		else {
			throw new PagingTreeException("unknown magic " + Integer.toHexString(magic) + " for page " + pageNum);
		}
		
	}

	private Object doRemove(Object key) {
		
		long pageNum = treeAddress.getPageNum();
		if (pageNum == 0) {
			return null;
		}
		
		PageReader pageReader = new PageReader(paging, pageNum);
		char magic = pageReader.readChar();
		
		if (magic == MagicCodes.INNER_NODE.getMagic()) {
			throw new IllegalStateException("unsupported INNER_NODE");
		}
		else if (magic == MagicCodes.LEAF_NODE.getMagic())  {
			return leafNode.remove(pageReader, key);
		}
		else {
			throw new PagingTreeException("unknown magic " + Integer.toHexString(magic) + " for page " + pageNum);
		}
		
	}
	
	private boolean doRemove(Object key, Object oldValue) {
		
		long pageNum = treeAddress.getPageNum();
		if (pageNum == 0) {
			return false;
		}
		
		PageReader pageReader = new PageReader(paging, pageNum);
		char magic = pageReader.readChar();
		
		if (magic == MagicCodes.INNER_NODE.getMagic()) {
			throw new IllegalStateException("unsupported INNER_NODE");
		}
		else if (magic == MagicCodes.LEAF_NODE.getMagic())  {
			return leafNode.remove(pageReader, key, oldValue);
		}
		else {
			throw new PagingTreeException("unknown magic " + Integer.toHexString(magic) + " for page " + pageNum);
		}
		
	}
	
	public void printGraph(PrintStream ps) {
		
		long pageNum = treeAddress.getPageNum();
		if (pageNum == 0) {
			return;
		}
		
		PageReader pageReader = new PageReader(paging, pageNum);
		char magic = pageReader.readChar();
		
		if (magic == MagicCodes.INNER_NODE.getMagic()) {
			throw new IllegalStateException("unsupported INNER_NODE");
		}
		else if (magic == MagicCodes.LEAF_NODE.getMagic())  {
			leafNode.printGraph(pageReader, ps);
		}
		else {
			throw new PagingTreeException("unknown magic " + Integer.toHexString(magic) + " for page " + pageNum);
		}
		
		
	}
	
	public interface ValuePredicate {
		
		boolean apply(boolean valueExists);
		
	}
	
	private static final ValuePredicate PUT = new ValuePredicate() {

		@Override
		public boolean apply(boolean valueExists) {
			return true;
		}
		
	};

	private static final ValuePredicate PUT_IF_ABSENT = new ValuePredicate() {

		@Override
		public boolean apply(boolean valueExists) {
			return !valueExists;
		}
		
	};

	private static final ValuePredicate REPLACE = new ValuePredicate() {

		@Override
		public boolean apply(boolean valueExists) {
			return valueExists;
		}
		
	};

}
