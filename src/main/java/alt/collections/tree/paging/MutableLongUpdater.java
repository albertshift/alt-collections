package alt.collections.tree.paging;

import alt.collections.concurrent.LongCas;
import alt.collections.util.ThreadUtil;

/**
 * Mutable Long Updater is using to atomically update value by compare-and-swap lock-free user algorithm.
 * 
 * @author Albert Shift
 *
 */

public interface MutableLongUpdater {

	/**
	 * Update value atomically
	 * @param longCas
	 * @return old value
	 */
	
	MutableLong update(LongCas longCas);
	
	/**
	 * INCREMENTAL UPDATER
	 */
	
	public static final MutableLongUpdater INCREMENTAL = new MutableLongUpdater() {

		@Override
		public MutableLong update(LongCas longCas) {
			while(true) {
				long oldValue = longCas.getLong();
				if (longCas.casLong(oldValue, oldValue+1)) {
					return new MutableLong(oldValue);
				}
				ThreadUtil.loopSleep();
			}
		}
		
	};
	
}
