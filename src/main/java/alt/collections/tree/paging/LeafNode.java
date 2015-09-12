package alt.collections.tree.paging;

import java.io.PrintStream;

import alt.collections.concurrent.LongCas;
import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.paging.Paging;
import alt.collections.tree.paging.PagingTree.ValuePredicate;

/**
 * Stateless Service class that encapsulate high-level scope algorithms that are working in LeafNode
 * 
 * @author Albert Shift
 *
 */

public final class LeafNode {

	private Paging paging;
	
	public LeafNode(Paging paging) {
		this.paging = paging;
	}

	/**
	 * Finds and returns value for the key
	 * 
	 * @param pageReader
	 * @param key
	 * @return
	 */
	
	public Object get(PageReader pageReader, Object key) {
		
		int pos = LeafNodePage.search(paging, pageReader, key);
		
		if (pos > 0) {
			// exact match
			return LeafNodePage.readValue(paging, pageReader, pos);
		}
		
		return null;
	}
	
	/**
	 * Puts entry to the tree. 
	 * If entry exists then value will be replaced and old value will be returned.
	 * If entry does not exist then new entry will be added, null will be returned.
	 * 
	 * @param pageReader
	 * @param key
	 * @param value
	 * @return
	 */
	
	public Object put(PageReader pageReader, Object key, Object value, ValuePredicate valuePredicate) {

		int pos = LeafNodePage.search(paging, pageReader, key);
		
		int requiredSize;
		if (pos > 0) {
			// exact match
			
			if (!valuePredicate.apply(true)) {
				// return old value
				return LeafNodePage.readValue(paging, pageReader, pos);
			}
			
			if (value instanceof MutableLong || value instanceof MutableLongUpdater) {
				PageWriter pageWriter = new PageWriter(pageReader);
				MutableLong oldValue = LeafNodePage.tryUpdateMutableLongValue(paging, pageReader, pageWriter, pos, value);
				if (oldValue != null) {
					return oldValue;
				}
			}

			requiredSize = LeafNodePage.estimateValueSize(value);
		}
		else {

			if (!valuePredicate.apply(false)) {
				return null;
			}

			requiredSize = LeafNodePage.estimateEntrySize(paging, key, value);
		}


		int	allocatedPos = LeafNodePage.tryAllocate(paging, pageReader, requiredSize);
		
		if (allocatedPos == -1) {
			throw new PagingTreeException("need to split");
		}

		PageWriter pageWriter = new PageWriter(pageReader);

		while(true) {
		
			pageWriter.seek(allocatedPos);
			
			if (pos > 0) {
				LeafNodePage.writeValue(paging, pageWriter, value);
				return LeafNodePage.updateValue(paging, pageReader, pos, allocatedPos);
			}
			else {
				LeafNodePage.writeEntry(paging, pageWriter, key, value);
				if (LeafNodePage.tryUpdateEntry(paging, pageWriter, -pos, allocatedPos)) {
					return null;
				}
				pos = LeafNodePage.search(paging, pageReader, key);
			}
		
		}
	}
	
	public boolean replace(PageReader pageReader, Object key, Object oldValue, Object newValue) {

		int pos = LeafNodePage.search(paging, pageReader, key);
		
		if (pos > 0) {
			// exact match
			
			if (oldValue instanceof MutableLong && newValue instanceof MutableLong) {
				final MutableLong oldValueMutableLong = (MutableLong) oldValue;
				final MutableLong newValueMutableLong = (MutableLong) newValue;
				PageWriter pageWriter = new PageWriter(pageReader);
				return null != LeafNodePage.tryUpdateMutableLongValue(paging, pageReader, pageWriter, pos, new MutableLongUpdater() {

					@Override
					public MutableLong update(LongCas longCas) {
						if (longCas.casLong(oldValueMutableLong.longValue(), newValueMutableLong.longValue())) {
							return oldValueMutableLong;
						}
						return null;
					}
					
				});
			}
			
			if (!LeafNodePage.isEqualsValue(paging, pageReader, pos, oldValue)) {
				return false;
			}
			
			int requiredSize = LeafNodePage.estimateValueSize(newValue);
			
			int	allocatedPos = LeafNodePage.tryAllocate(paging, pageReader, requiredSize);
			
			if (allocatedPos == -1) {
				throw new PagingTreeException("need to split");
			}

			return LeafNodePage.updateValue(paging, pageReader, pos, oldValue, allocatedPos);
		}
		
		return false;
	}
	
	/**
	 * Removes entry with the key, does not consume memory
	 * 
	 * @param pageReader
	 * @param key
	 * @return
	 */
	
	public Object remove(PageReader pageReader, Object key) {
	
		int pos = LeafNodePage.search(paging, pageReader, key);
		
		if (pos > 0) {
			// exact match
			return LeafNodePage.updateValue(paging, pageReader, pos, 0);
			
		}
		
		return null;
		
	}
	
	/**
	 * Removes entry with the key, checks old value
	 * 
	 * @param pageReader
	 * @param key
	 * @param oldValue
	 * @return
	 */
	
	public boolean remove(PageReader pageReader, Object key, Object oldValue) {
		
		int pos = LeafNodePage.search(paging, pageReader, key);
		
		if (pos > 0) {
			// exact match
			return LeafNodePage.updateValue(paging, pageReader, pos, oldValue, 0);
			
		}
		
		return false;
		
	}
	
	
	public void printGraph(PageReader pageReader, PrintStream ps) {
		
		ps.println("digraph{");
		
		// seek to first entry
		int entryPos = LeafNodePage.getFirstEntryPos(paging);
		
		printLeafEntry(pageReader, entryPos, ps, null);
		
		ps.println("}");
		
	}
	
	private static int globalStaticInt = 1;
	
	private String printLeafEntry(PageReader pageReader, int entryPos, PrintStream ps, String parent) {
		
		pageReader.seek(LeafNodePage.getKeyPos(paging, entryPos));
		Object key = ValueHolder.readValue(pageReader);

		pageReader.seek(LeafNodePage.getValuePos(paging, entryPos));
		int valuePos = pageReader.readInnerRef();
		pageReader.seek(valuePos);
		Object value = ValueHolder.readValue(pageReader);

		String me = "\"" + key + ":" + value + "\"";
		
		String childLesser = "null" + Integer.toString(globalStaticInt++);
		String childGreater = "null" + Integer.toString(globalStaticInt++);
		
		int innerRef = LeafNodePage.getLesserPos(entryPos);
		pageReader.seek(innerRef);
		int pos = pageReader.readInnerRef();
		if (pos != 0) {
			childLesser = printLeafEntry(pageReader, pos, ps, me);
		}
		
		innerRef = LeafNodePage.getGreaterPos(paging, entryPos);
		pageReader.seek(innerRef);
		pos = pageReader.readInnerRef();
		if (pos != 0) {
			childGreater = printLeafEntry(pageReader, pos, ps, me);
		}
		

		ps.println(me + " -> { " + childLesser + " " + childGreater + " }");
		
		
		return me;
		
	}
}
