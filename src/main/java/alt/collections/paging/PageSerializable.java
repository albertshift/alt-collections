package alt.collections.paging;


/**
 * Basic interface for all data that can be serialized to the Page
 * 
 * @author Albert Shift
 *
 */

public interface PageSerializable {

	/**
	 * Estimates size of the serialized data
	 * 
	 * @return size in bytes
	 */
	
	int sizeOf();
	
	/**
	 * Serializes object to the Page
	 * 
	 * @param writer
	 */
	
	void writeData(PageWriter writer);
	
	/**
	 * Reads object from the Page
	 * 
	 * @param reader
	 */
	
	void readData(PageReader reader);
	
}
