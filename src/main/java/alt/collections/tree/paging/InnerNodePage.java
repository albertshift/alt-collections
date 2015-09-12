package alt.collections.tree.paging;

/**
 * InnerNode Page stores only Keys and childs
 * 
 * [magic:char], [pageTail:innerRef], [lastChild:pageNum], [heapSpace:heap]
 * 
 * Entry schema:
 * 
 * [lesser:innerRef], [greater:innerRef], [lesserChild:pageNum], [keyType:byte], [keySize:vLong:optional], [key:bytes]
 * [lesser:innerRef], [greater:innerRef], [lesserChild:pageNum], [keyType:byte], [dataPage:pageNum]
 * 
 * @author Albert Shift
 *
 */

public final class InnerNodePage {

}
