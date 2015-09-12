package alt.collections.util;

public enum MapFileMode {

	READ_ONLY(0),
	READ_WRITE(1),
	PRIVATE(2);

	private final int value;
	
	private MapFileMode(int v) {
		this.value = v;
	}

	public int getValue() {
		return value;
	}
	
	
}
