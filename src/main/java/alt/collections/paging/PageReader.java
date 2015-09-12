package alt.collections.paging;

import alt.collections.concurrent.InnerRefCas;
import alt.collections.concurrent.IntegerCas;
import alt.collections.concurrent.LongCas;
import alt.collections.concurrent.PageNumCas;
import alt.collections.util.BigEndian;
import alt.collections.util.Requires;
import alt.collections.util.Unsafe;

/**
 * Common Page Reader for all algorithms
 * 
 * @author Albert Shift
 *
 */

public final class PageReader extends PageStream {

    public PageReader(Paging paging) {
    	super(paging);
    }
    
    public PageReader(Paging paging, long pageNum) {
    	super(paging, pageNum);
    }
    
    public PageReader(PageStream pageStream) {
    	super(pageStream);
    }

    public byte readByte() {
    	ensureCanGrow(1);
    	byte value = Unsafe.INSTANCE.getByte(address + position);
    	position++;
    	return value;
    }
    
    public char readChar() {
    	ensureCanGrow(2);
    	char value = Unsafe.INSTANCE.getChar(address + position);
    	position += 2;
    	return BigEndian.ioChar(value);
    }
    
    public short readShort() {
    	ensureCanGrow(2);
    	short value = Unsafe.INSTANCE.getShort(address + position);
    	position += 2;
    	return BigEndian.ioShort(value);
    }

    public int readInt() {
    	ensureCanGrow(4);
    	int value = Unsafe.INSTANCE.getInt(address + position);
    	position += 4;
    	return BigEndian.ioInt(value);
    }

    public long readLong() {
    	ensureCanGrow(8);
    	long value = Unsafe.INSTANCE.getLong(address + position);
    	position += 8;
    	return BigEndian.ioLong(value);
    }
    
    public float readFloat() {
    	ensureCanGrow(4);
    	float value = Unsafe.INSTANCE.getFloat(address + position);
    	position += 4;
    	return BigEndian.ioFloat(value);
    }
    
    public double readDouble() {
    	ensureCanGrow(8);
    	double value = Unsafe.INSTANCE.getDouble(address + position);
    	position += 8;
    	return BigEndian.ioDouble(value);
    }
    
	public long readVLong() {
		ensureCanGrow(1);
		int unsignedByte = readByte();
		long lvalue = unsignedByte & 0x3F;
		int numBits = 6;
		boolean isNegative = (unsignedByte & 0x40) != 0;
		while((unsignedByte & 0x80) != 0) {
			ensureCanGrow(1);
			unsignedByte = readByte();
			lvalue |= (long)(unsignedByte & 0x7F) << numBits;
			numBits += 7;
		}
		return isNegative ? lvalue ^ -1L : lvalue;
	}
	
	public String readString() {
		int length = (int) readVLong();
		Requires.range(length, 0, paging.getPageSize(), "length");
		
		char[] buffer = new char[length];
		readUtf8To(buffer, 0, length);
		return new String(buffer);
	}

	public char readUtf8Char() {
		
		int b = readByte() & 0xFF;
		
		int c = b >> 4;

		if (c <= 7) {
			return ((char) b);
		}
		
		if (c == 12 || c == 13) {
			int b2 = readByte();
			return ( (char)((b & 0x1F) << 6 | b2 & 0x3F) );
		}
		
		if (c == 14) {
			int b2 = readByte();
			int b3 = readByte();
			return ( (char)((b & 0x0F) << 12 | (b2 & 0x3F) << 6 | b3 & 0x3F) );
		}
		
		if (c == 15) {
			int l = (b >> 1) & 0x07;
			if (l <= 3) {
				ensureCanGrow(3);
				position += 3;
			}
			else if (l <= 5) {
				ensureCanGrow(4);
				position += 4;
			}
			else {
				ensureCanGrow(5);
				position += 5;
			}
		}
		return 0x3F;
	}
	
	public void readUtf8To(char[] buffer, int offset, int length) {
		
		for (int i = 0; i != length; ++i) {
			buffer[i + offset] = readUtf8Char();
		}
		
	}
	
	public byte[] readBytes() {
		int len = (int) readVLong();
		Requires.range(len, 0, paging.getPageSize(), "len");
		
		ensureCanGrow(len);
		byte[] blob = new byte[len];
		Unsafe.INSTANCE.copyMemory(null, address + position, blob, Unsafe.BYTEARRAY_BASEOFFSET, len);
		position += len;
		return blob;
	}
	
	public void readBytesTo(byte[] buffer, int offset, int length) {
		Requires.nonNull(buffer, "buffer");
		Requires.positive(offset, "offset");
		Requires.positive(length, "length");
		
		ensureCanGrow(length);
		Unsafe.INSTANCE.copyMemory(null, address + position, buffer, Unsafe.BYTEARRAY_BASEOFFSET + offset * Unsafe.BYTEARRAY_INDEXSCALE, length);
		position += length;
	}

	public int readInnerRef() {
		InnerRef innerRef = paging.getInnerRef();
		
		ensureCanGrow(innerRef.size());
		
		int result = innerRef.readInnerRef(address + position);
		Requires.range(result, 0, paging.getPageSize(), "result");
		
		position += innerRef.size();
		return result;
	}

	public long readPageNum() {
		PageNum pageNum = paging.getPageNum();
		
		ensureCanGrow(pageNum.size());
		
		long result = pageNum.readPageNum(address + position);
		Requires.positive(result, "result");
		
		position += pageNum.size();
		return result;
	}
	
	public IntegerCas readIntegerCas() {
    	ensureCanGrow(4);
    	IntegerCas result = new IntegerCas(address, position);
    	position += 4;
    	return result;
	}
	
	public LongCas readLongCas() {
    	ensureCanGrow(8);
    	LongCas result = new LongCas(address, position);
    	position += 8;
    	return result;
	}
	
	public InnerRefCas readInnerRefCas() {
		InnerRef innerRef = paging.getInnerRef();
    	ensureCanGrow(innerRef.size());
    	InnerRefCas result = new InnerRefCas(address, position, innerRef);
    	position += innerRef.size();
    	return result;
	}

	public PageNumCas readPageNumCas() {
		PageNum pageNum = paging.getPageNum();
    	ensureCanGrow(pageNum.size());
    	PageNumCas result = new PageNumCas(address, position, pageNum);
    	position += pageNum.size();
    	return result;
	}
	
	public int compareToString(String str) {
		int len1 = (int) readVLong();
		int len2 = str.length();
		int n = Math.min(len1, len2);
	    for (int i = 0; i != n; ++i) {
			char c1 = readUtf8Char();
			char c2 = str.charAt(i);
			if (c1 != c2) {
			    return c1 - c2;
			}
	    }
	    return len1 - len2;
	}
	
}
