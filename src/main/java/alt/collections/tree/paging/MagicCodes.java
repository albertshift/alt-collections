package alt.collections.tree.paging;

/**
 * Magic codes for different pages
 * 
 * @author Albert Shift
 *
 */

public enum MagicCodes {

	NEW_STORE((char) 0), 
	MASTER((char) 0x5555), 
	MASTER_CONTINUE((char) 0x7777), 
	INNER_NODE((char) 0xAAAA), 
	LEAF_NODE((char) 0xBBBB), 
	DATA((char) 0xCCCC),
	DATA_CONTINUE((char) 0xDDDD);
	
	private char magic;
	
	private MagicCodes(char magic) {
		this.magic = magic;
	}

	public char getMagic() {
		return magic;
	}

}
