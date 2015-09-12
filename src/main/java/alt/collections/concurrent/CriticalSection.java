package alt.collections.concurrent;

import alt.collections.util.JniThreadUtil;
import alt.collections.util.ThreadUtil;

/**
 * Simple algorithm to work in critical sections
 * 
 * @author Albert Shift
 *
 */

public class CriticalSection {
	
	private final IntegerCas integerCas;
	private final int defaultValue;

	public CriticalSection(IntegerCas integerCas, int defaultValue) {
		this.integerCas = integerCas;
		this.defaultValue = defaultValue;
	}
	
	public <T> T execute(Callback<T> cb) {
		
		while(true) {
		
			if (integerCas.casInt(defaultValue, defaultValue + 1)) {
				try {
					//System.out.println("counter = " + counter);
					return cb.doInCriticalSection();
				}
				finally {
					integerCas.putInt(defaultValue);
				}
			}
			
			ThreadUtil.loopSleep();
		
		}
		
	}
	
	public interface Callback<T> {
		
		T doInCriticalSection();
		
	}
	
	
}
