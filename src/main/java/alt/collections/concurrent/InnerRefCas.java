package alt.collections.concurrent;

import alt.collections.paging.InnerRef;

/**
 * Atomic Operations under the InnerRef
 * 
 * @author Albert Shift
 *
 */

public class InnerRefCas {

	private final long address;
	private final int ref;
	private final InnerRef innerRef;

	public InnerRefCas(long address, int ref, InnerRef innerRef) {
		this.address = address;
		this.ref = ref;
		this.innerRef = innerRef;
	}
	
	public int getInnerRef() {
		return innerRef.readInnerRef(address + ref);
	}

	public void putInnerRef(int value) {
		this.innerRef.writeInnerRef(address + ref, value);
	}
	
	public boolean casInnerRef(int oldValue, int newValue) {
		return innerRef.casInnerRef(address + ref, oldValue, newValue);
	}
	
	
}
