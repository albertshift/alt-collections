package alt.collections.tree.paging;

/**
 * Value Type represents known specific value
 * 
 * 
 * @author Albert Shift
 *
 */

public enum ValueType {
	
	DATAPAGE((byte)'d'),
	LONG((byte)'l'),
	STRING((byte)'s'),
	BLOB((byte)'b'),
	MUTABLE_LONG((byte)'m');
	
	private byte value;
	
	private ValueType(byte value) {
		this.value = value;
	}

	public byte getType() {
		return value;
	}

}
