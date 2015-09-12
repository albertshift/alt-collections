package alt.collections.tree.paging;

import java.util.List;

import alt.collections.concurrent.PageNumCas;

/**
 * Root mapping interface for the Tree that only grows.
 * 
 * This is a store tree of trees.
 * 
 * Key: String. Tree name
 * Value: PageNumCas. Reference to the root node of the user tree
 * 
 * @author Albert Shift
 *
 */

public interface NamedTreeMap {

	PageNumCas findOrCreate(String treeName); 
	
	PageNumCas find(String treeName);
	
	List<String> getAllNames();
	
}
