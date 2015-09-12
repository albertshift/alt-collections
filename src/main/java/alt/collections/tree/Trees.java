package alt.collections.tree;

import alt.collections.paging.Paging;


/**
 * Trees
 * 
 * @author Albert Shift
 *
 */
public class Trees {

	public static BTreeBuilder btree() {
		return new BTreeBuilder();
	}
	
	public static PagingTreeBuilder pagingTree() {
		return new PagingTreeBuilder();
	}
	
	public static PagingTreeBuilder pagingTree(Paging paging, String treeName) {
		return new PagingTreeBuilder().usePaging(paging).useTreeName(treeName);
	}
	
}
