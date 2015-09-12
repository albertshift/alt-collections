package alt.collections.tree.paging;

import alt.collections.concurrent.InnerRefCas;
import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.paging.Paging;
import alt.collections.util.ThreadUtil;

/**
 * LeafNode Page stores Keys and Values
 * 
 * [magic:char], [pageTail:innerRef], [lastValue:innerRef], [heapSpace:heap]
 * 
 * Entry schema:
 * 
 * [lesser:innerRef], [greater:innerRef], [value:innerRef], [key:valueHolder]
 * 
 * @author Albert Shift
 *
 */

public final class LeafNodePage {

	public static int getFirstEntryPos(Paging paging) {
		return 2 + 2 * paging.getInnerRef().size();
	}
	
	public static int getLesserPos(int entryPos) {
		return entryPos;
	}

	public static int getGreaterPos(Paging paging, int entryPos) {
		return entryPos + paging.getInnerRef().size();
	}

	public static int getValuePos(Paging paging, int entryPos) {
		return entryPos + 2 * paging.getInnerRef().size();
	}
	
	public static int getKeyPos(Paging paging, int entryPos) {
		return entryPos + 3 * paging.getInnerRef().size();
	}
	
	public static int estimateEntrySize(Paging paging, Object key, Object value) {
		return 3 * paging.getInnerRef().size() + ValueHolder.estimateSize(key) + ValueHolder.estimateSize(value);
	}

	public static int estimateValueSize(Object value) {
		return ValueHolder.estimateSize(value);
	}

	/**
	 * Searches entry in the LeafNode by using key. PageReader must by pointed to the first entry.
	 * 
	 * @param paging
	 * @param pageReader
	 * @param key
	 * @return negative - is the position of the lesser or greater innerRef to add new entry
	 *         positive - is the position of the value inner reference inside the entry
	 */
	
	public static int search(Paging paging, PageReader pageReader, Object key) {
		
		// seek to first entry
		int entryPos = getFirstEntryPos(paging);
		
		while(true) {
		
			pageReader.seek(getKeyPos(paging, entryPos));
			
			int c = ValueHolder.compareTo(pageReader, key);
			
			if (c > 0) {
				pageReader.seek(getLesserPos(entryPos));
				int lesserEntryPos = pageReader.readInnerRef();
				if (lesserEntryPos != 0) {
					entryPos = lesserEntryPos;
					continue;
				}
				return -getLesserPos(entryPos);
			}
			else if (c < 0) { 
				pageReader.seek(getGreaterPos(paging, entryPos));
				int greaterEntryPos = pageReader.readInnerRef();
				if (greaterEntryPos != 0) {
					entryPos = greaterEntryPos;
					continue;
				}
				return -getGreaterPos(paging, entryPos);				
			}
			else {
				// exact match
				return getValuePos(paging, entryPos);
			}
		
		}
		
	}
	
	/**
	 * Tries to allocate heap memory in the page
	 * 
	 * @param paging
	 * @param pageReader
	 * @param size
	 * @return positive integer value for success, that is position of the memory block in the page
	 *         negative integer value shows that there is no space in the page
	 */
	
	
	public static int tryAllocate(Paging paging, PageReader pageReader, int size) {
		pageReader.seek(2);
		InnerRefCas pageTail = pageReader.readInnerRefCas();
		while(true) {
			int tail = pageTail.getInnerRef();
			if (tail + size > paging.getPageSize()) {
				return -1;
			}
			if (pageTail.casInnerRef(tail, tail + size)) {
				return tail;
			}
		}
	}
	
	/**
	 * Formats new page, writes header of the page.
	 * 
	 * @param paging
	 * @param pageNum
	 * @param key
	 * @param value
	 */
	
	public static void structBlank(Paging paging, long pageNum, Object key, Object value) {

		PageWriter pageWriter = new PageWriter(paging, pageNum);
		
		// magic
		pageWriter.writeChar(MagicCodes.LEAF_NODE.getMagic()); 

		// pageTail
		int pageTailPos = pageWriter.getPositionAndSkipInnerRef();

		// lastValue
		pageWriter.writeInnerRef(0);
		
		// first entry
		writeEntry(paging, pageWriter, key, value);
		
		int tail = pageWriter.getPosition();
		pageWriter.seek(pageTailPos);
		pageWriter.writeInnerRef(tail);
		
	}
	
	/**
	 * Tries to update mutable long value
	 * 
	 * @param paging
	 * @param pageReader
	 * @param pageWriter
	 * @param valuePosInnerRef
	 * @param newValue
	 * @return not null old value if updated 
	 */
	
	public static MutableLong tryUpdateMutableLongValue(Paging paging, PageReader pageReader, PageWriter pageWriter, int valuePosInnerRef, Object newValue) {
		
		pageReader.seek(valuePosInnerRef);
		
		int valuePos = pageReader.readInnerRef();
		
		if (valuePos == 0) {
			return null;
		}
		
		pageReader.seek(valuePos);
		ValueType vt = ValueHolder.getValueType(pageReader);
		if (vt != ValueType.MUTABLE_LONG) {
			return null;
		}
		
		pageReader.seek(valuePos);
		pageWriter.seek(valuePos);
		return ValueHolder.updateMutableLong(pageReader, pageWriter, newValue);
	}
	
	/**
	 * Reads value
	 * 
	 *   valuePosInnerRef
	 *         \/
	 * [entry, *valueInnerRef ----> [value] ]
	 * 
	 * @param paging
	 * @param pageReader
	 * @param valuePosInnerRef
	 * @return
	 */
	
	public static Object readValue(Paging paging, PageReader pageReader, int valuePosInnerRef) {
		
		pageReader.seek(valuePosInnerRef);
		int valuePos = pageReader.readInnerRef();
		
		if (valuePos != 0) {
			
			pageReader.seek(valuePos);
			return ValueHolder.readValue(pageReader);

		}
		
		return null;
	}
	
	/**
	 * Compare existing value
	 * 
	 * @param paging
	 * @param pageReader
	 * @param valuePosInnerRef
	 * @param oldValue
	 * @return
	 */
	
	public static boolean isEqualsValue(Paging paging, PageReader pageReader, int valuePosInnerRef, Object oldValue) {

		pageReader.seek(valuePosInnerRef);
		int valuePos = pageReader.readInnerRef();
		
		if (valuePos != 0) {
			
			pageReader.seek(valuePos);
			return ValueHolder.compareTo(pageReader, oldValue) == 0;

		}
		
		return false;

	}
	
	/**
	 * Updates value inner reference in the entry to the new value position.
	 * 
	 *   valuePosInnerRef       newValuePos
	 *         \/                   \/
	 * [entry, *valueInnerRef ----> [newValue] ]
	 * 
	 * @param paging
	 * @param pageReader
	 * @param valuePosInnerRef
	 * @param newValuePos
	 */
	
	public static final Object updateValue(Paging paging, PageReader pageReader, int valuePosInnerRef, int newValuePos) {
		
		pageReader.seek(valuePosInnerRef);
		InnerRefCas valueRefCas = pageReader.readInnerRefCas();
		
		int oldValuePos = 0;
		while(true) {

			oldValuePos = valueRefCas.getInnerRef();
			
			if (valueRefCas.casInnerRef(oldValuePos, newValuePos)) {
				break;
			}
			
			ThreadUtil.loopSleep();
		}
		
		if (oldValuePos != 0) {
			
			pageReader.seek(oldValuePos);
			return ValueHolder.readValue(pageReader);
			
		}
		
		return null;
	}
	
	/**
	 * Update value
	 * 
	 * @param paging
	 * @param pageReader
	 * @param valuePosInnerRef
	 * @param oldValue
	 * @param newValuePos
	 * @return
	 */
	
	public static final boolean updateValue(Paging paging, PageReader pageReader, int valuePosInnerRef, Object oldValue, int newValuePos) {
		
		pageReader.seek(valuePosInnerRef);
		InnerRefCas valueRefCas = pageReader.readInnerRefCas();
		
		while(true) {

			int oldValuePos = valueRefCas.getInnerRef();
			
			if (oldValuePos == 0 && oldValue != null) {
				return false;
			}

			if (oldValuePos != 0 && oldValue == null) {
				return false;
			}

			if (oldValuePos != 0 && oldValue != null) {
				pageReader.seek(oldValuePos);
				if (0 != ValueHolder.compareTo(pageReader, oldValue)) {
					return false;
				}
			}
			
			if (valueRefCas.casInnerRef(oldValuePos, newValuePos)) {
				return true;
			}
			
			ThreadUtil.loopSleep();
		}
		
	}
	
	/**
	 * Updates entry inner reference in the innerRef of another entry
	 * 
	 *     entryPosInnerRef             newEntryPos
	 *           \/                        \/
	 * [ entry1, *lesser or greater -----> [entry2]  ]
	 * 
	 * @param paging
	 * @param pageWriter
	 * @param entryPosInnerRef
	 * @param newEntryPos
	 * @return
	 */
	
	public static final boolean tryUpdateEntry(Paging paging, PageWriter pageWriter, int entryPosInnerRef, int newEntryPos) {
	
		pageWriter.seek(entryPosInnerRef);
		
		InnerRefCas innerRefCas = pageWriter.writeInnerRefCas();
		
		return innerRefCas.casInnerRef(0, newEntryPos);
		
	}
	
	/**
	 * Writes value in the page, memory must be preallocated
	 * 
	 * @param paging
	 * @param pageWriter must be pointed to the value position
	 * @param value
	 */
	
	public static final void writeValue(Paging paging, PageWriter pageWriter, Object value) {
		
		// value
		ValueHolder.writeValue(pageWriter, value);
		
	}
	
	/**
	 * Writes blank entry to the page, memory must be preallocated
	 * 
	 * @param paging
	 * @param pageWriter must be pointed to the entry position
	 * @param key
	 * @param value
	 */
	
	public static final void writeEntry(Paging paging, PageWriter pageWriter, Object key, Object value) {
		
		// lesser
		pageWriter.writeInnerRef(0);
		
		// greater
		pageWriter.writeInnerRef(0);
		
		// value ref
		int valueRefPos = pageWriter.getPositionAndSkipInnerRef();
		
		// key
		ValueHolder.writeValue(pageWriter, key);
		
		int valuePos = pageWriter.getPosition();
		
		// value
		ValueHolder.writeValue(pageWriter, value);
		
		// write value ref
		int savePosition = pageWriter.getPosition();
		
		pageWriter.seek(valueRefPos);
		pageWriter.writeInnerRef(valuePos);
		
		pageWriter.seek(savePosition);
	}
	
}
