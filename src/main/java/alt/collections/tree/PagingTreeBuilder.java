package alt.collections.tree;

import alt.collections.paging.Paging;
import alt.collections.tree.paging.PagingTree;
import alt.collections.util.Requires;


/**
 * Paging Tree Builder
 * 
 * @author Albert Shift
 *
 */

public class PagingTreeBuilder {

	private Paging paging;
	private String treeName;
	
	public PagingTreeBuilder usePaging(Paging paging) {
		this.paging = paging;
		return this;
	}

	public PagingTreeBuilder useTreeName(String treeName) {
		this.treeName = treeName;
		return this;
	}

	public <K, V> PagingTree<K, V> build() {
		Requires.nonNull(paging, "paging");
		Requires.nonNull(treeName, "treeName");
		return new PagingTree<K, V>(paging, treeName);
	}
	
}
