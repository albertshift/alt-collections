package alt.collections.tree.paging;

import alt.collections.paging.PageReader;
import alt.collections.paging.PageWriter;
import alt.collections.util.Requires;

/**
 * Value holder is basic class to store keys and values in the Inner/Lead Nodes
 * 
 * Value schema:
 * 
 * [valueType:byte], [sizeInBytes:vLong:optional], [value:bytes]
 * [valueType:byte], [dataPage:pageNum], [nextDataPageRef:innerRef]
 * 
 * Examples:
 * 
 * STRING:
 * [ValueType.STRING:byte], [sizeInBytes:vLong], [utf8:bytes]
 * 
 * LONG:
 * [ValueType.LONG:byte], [value:vLong]
 * 
 * MUTABLE_LONG:
 * [ValueType.ATOMIC_LONG:byte], [value:long]
 *
 * BLOB:
 * [ValueType.BLOB:byte], [sizeInBytes:vLong], [blob:bytes]
 *
 * DATAPAGE:
 * [ValueType.DATAPAGE:byte], [dataPage:pageNum], [nextDataPageRef:innerRef]
 *
 * @author Albert Shift
 *
 */

public final class ValueHolder {
	
	public static final int VALUETYPE_SIZE = 1;
	public static final int MUTABLELONG_SIZE = 8;
	
	private static final TypedValue valueTypeHolders[] = new TypedValue[getMaxValueType()+1];
	private static final MutableLongValue MUTABLE_LONG_VALUE = new MutableLongValue();
	
	static {
		valueTypeHolders[ValueType.STRING.ordinal()] = new StringValue();
		valueTypeHolders[ValueType.LONG.ordinal()] = new LongValue();
		valueTypeHolders[ValueType.MUTABLE_LONG.ordinal()] = MUTABLE_LONG_VALUE;
		valueTypeHolders[ValueType.BLOB.ordinal()] = new BlobValue();
	}
	
	public static MutableLong updateMutableLong(PageReader reader, PageWriter writer, Object newValue) {
		reader.skip(1);
		writer.skip(1);
		return MUTABLE_LONG_VALUE.update(reader, writer, newValue);
	}
	
	public static int compareTo(PageReader pageReader, Object key) {
		byte valueType = pageReader.readByte();
		ValueType vt = valueOf(valueType);
		ValueType keyVt = detectValueType(key);
		if (vt == keyVt) {
			return valueTypeHolders[vt.ordinal()].compareTo(pageReader, key);
		}
		else {
			return vt.ordinal() - keyVt.ordinal();
		}
	}
	
	public static ValueType getValueType(PageReader pageReader) {
		byte valueType = pageReader.readByte();
		return valueOf(valueType);
	}
	
	public static int estimateSize(Object value) {
		ValueType vt = detectValueType(value);
		return VALUETYPE_SIZE + valueTypeHolders[vt.ordinal()].estimateSize(value);
	}
	
	public static void writeValue(PageWriter pageWriter, Object value) {
		ValueType vt = detectValueType(value);
		pageWriter.writeByte(vt.getType());
		valueTypeHolders[vt.ordinal()].write(pageWriter, value);
	}
	
	public static Object readValue(PageReader pageReader) {
		byte valueType = pageReader.readByte();
		ValueType vt = valueOf(valueType);
		return valueTypeHolders[vt.ordinal()].read(pageReader);
	}
	
	public static ValueType detectValueType(Object value) {
		if (value instanceof String) {
			return ValueType.STRING;
		}
		if (value instanceof Long) {
			return ValueType.LONG;
		}
		if (value instanceof MutableLong) {
			return ValueType.MUTABLE_LONG;
		}
		if (value instanceof byte[]) {
			return ValueType.BLOB;
		}
		throw new PagingTreeException("unknown value type " + value.getClass());
	}
	
	public static ValueType valueOf(byte valueType) {
		if (valueType == ValueType.STRING.getType()) {
			return ValueType.STRING;
		}
		if (valueType == ValueType.LONG.getType()) {
			return ValueType.LONG;
		}
		if (valueType == ValueType.MUTABLE_LONG.getType()) {
			return ValueType.MUTABLE_LONG;
		}
		if (valueType == ValueType.BLOB.getType()) {
			return ValueType.BLOB;
		}
		throw new PagingTreeException("unknown value type " + valueType);
	}

	public interface TypedValue {
		
		int estimateSize(Object value);
		
		void write(PageWriter writer, Object value);
		
		Object read(PageReader reader);
		
		int compareTo(PageReader reader, Object key);
		
	}
	
	public static class StringValue implements TypedValue {
	
		public int estimateSize(Object value) {
			String str = (String) value;
			int sizeInBytes = PageWriter.estimateUtf8Size(str, 0, str.length());
			return PageWriter.estimateVLongSize(sizeInBytes) + sizeInBytes;
		}
		
		public void write(PageWriter writer, Object value) {
			String str = (String) value;
			int sizeInBytes = PageWriter.estimateUtf8Size(str, 0, str.length());
			writer.writeVLong(sizeInBytes);
			writer.writeUtf8(str, 0, str.length());
		}
		
		public Object read(PageReader reader) {
			int sizeInBytes = (int) reader.readVLong();
			
			int endPosition = reader.getPosition() + sizeInBytes;
			
			Requires.positive(sizeInBytes, "sizeInBytes");
			char[] chars = new char[sizeInBytes];
			
			int i = 0;
			for (; i != chars.length && reader.getPosition() < endPosition; ++i) {
				chars[i] = reader.readUtf8Char();
			}
			
			return new String(chars, 0, i);
		}

		public int compareTo(PageReader reader, Object value) {
			String str = (String) value;
			
			int bytes1 = (int) reader.readVLong();
			Requires.positive(bytes1, "bytes1");
			Requires.lessOrEquals(bytes1, reader.pageSize().getPageSize(), "bytes1");
			
			int endPosition = reader.getPosition() + bytes1;
			
			int chars2 = str.length();
			
			int i = 0;
			for (; i != chars2; ++i) {
				
				if (reader.getPosition() >= endPosition) {
					return -1;
				}
				
				char c1 = reader.readUtf8Char();
				char c2 = str.charAt(i);
				if (c1 != c2) {
				    return c1 - c2;
				}
			}
			
			return endPosition - reader.getPosition();
		}

	}
	
	public static class LongValue implements TypedValue {
		
		public int estimateSize(Object value) {
			Long lvalue = (Long) value;
			return PageWriter.estimateVLongSize(lvalue.longValue());
		}
		
		public void write(PageWriter writer, Object value) {
			Long lvalue = (Long) value;
			writer.writeVLong(lvalue.longValue());
		}
		
		public Object read(PageReader reader) {
			return reader.readVLong();
		}
		
		public int compareTo(PageReader reader, Object value) {
			Long lvalue = (Long) value;
			long thisVal = reader.readVLong();
			long anotherVal = lvalue.longValue();
			return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
		}

	}
	
	public static class MutableLongValue implements TypedValue {
		
		public int estimateSize(Object value) {
			return MUTABLELONG_SIZE;
		}
		
		public void write(PageWriter writer, Object value) {
			MutableLong lvalue = (MutableLong) value;
			writer.writeLong(lvalue.longValue());
		}
		
		public Object read(PageReader reader) {
			return new MutableLong(reader.readLong());
		}
		
		public MutableLong update(PageReader reader, PageWriter writer, Object value) {
			if (value instanceof MutableLong) {
				MutableLong newValue = (MutableLong) value;
				long oldValue = reader.readLong();
				writer.writeLong(newValue.longValue());
				return new MutableLong(oldValue);
			}
			else if (value instanceof MutableLongUpdater) {
				MutableLongUpdater updater = (MutableLongUpdater) value;
				return updater.update(writer.writeLongCas());
			}
			else {
				throw new PagingTreeException("unknown value type " + value.getClass());
			}
		}
		
		public int compareTo(PageReader reader, Object value) {
			MutableLong lvalue = (MutableLong) value;
			long thisVal = reader.readLong();
			long anotherVal = lvalue.longValue();
			return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
		}
	}
	
	public static class BlobValue implements TypedValue {
		
		public int estimateSize(Object value) {
			byte[] blob = (byte[]) value;
			return  PageWriter.estimateVLongSize(blob.length) + blob.length;
		}
		
		public void write(PageWriter writer, Object value) {
			byte[] blob = (byte[]) value;
			writer.writeBytes(blob);
		}
		
		public Object read(PageReader reader) {
			return reader.readBytes();
		}
		
		public int compareTo(PageReader reader, Object value) {
			byte[] blob = (byte[]) value;
			int len1 = (int) reader.readVLong();
			Requires.positive(len1, "len1");
			Requires.lessOrEquals(len1, reader.pageSize().getPageSize(), "len1");
			int len2 = blob.length;
			int n = Math.min(len1, len2);
			for (int i = 0; i != n; ++i) {
				int b1 = reader.readByte() & 0xFF;
				int b2 = blob[i] & 0xFF;
				if (b1 != b2) {
				    return b1 - b2;
				}
			}
			return len1 - len2;
		}
		
	}
	
	private static final int getMaxValueType() {
		int max = 0;
		for (ValueType vt : ValueType.values()) {
			if (max < vt.ordinal()) {
				max = vt.ordinal();
			}
		}
		return max;
	}
	
}
