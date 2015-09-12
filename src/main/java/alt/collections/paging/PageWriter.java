package alt.collections.paging;

import alt.collections.concurrent.InnerRefCas;
import alt.collections.concurrent.IntegerCas;
import alt.collections.concurrent.LongCas;
import alt.collections.concurrent.PageNumCas;
import alt.collections.util.BigEndian;
import alt.collections.util.Requires;
import alt.collections.util.Unsafe;

/**
 * Common Page Writer for all algorithms
 * 
 * @author Albert Shift
 *
 */

public final class PageWriter extends PageStream {

    public PageWriter(Paging paging) {
    	super(paging);
    }
    
    public PageWriter(Paging paging, long pageNum) {
    	super(paging, pageNum);
    }

    public PageWriter(PageStream pageStream) {
    	super(pageStream);
    }
  
    public void writeByte(byte value) {
    	ensureCanGrow(1);
    	Unsafe.INSTANCE.putByte(address + position, value);
    	position++;
    }
    
    public void writeChar(char value) {
    	ensureCanGrow(2);
    	Unsafe.INSTANCE.putChar(address + position, BigEndian.ioChar(value));
    	position += 2;
    }

    public void writeShort(short value) {
    	ensureCanGrow(2);
    	Unsafe.INSTANCE.putShort(address + position, BigEndian.ioShort(value));
    	position += 2;
    }

    public void writeInt(int value) {
    	ensureCanGrow(4);
    	Unsafe.INSTANCE.putInt(address + position, BigEndian.ioInt(value));
    	position += 4;
    }

    public void writeLong(long value) {
    	ensureCanGrow(8);
    	Unsafe.INSTANCE.putLong(address + position, BigEndian.ioLong(value));
    	position += 8;
    }

    public void writeFloat(float value) {
    	ensureCanGrow(4);
    	Unsafe.INSTANCE.putFloat(address + position, BigEndian.ioFloat(value));
    	position += 4;
    }

    public void writeDouble(double value) {
    	ensureCanGrow(8);
    	Unsafe.INSTANCE.putDouble(address + position, BigEndian.ioDouble(value));
    	position += 8;
    }

	public void writeVLong(long lvalue) {
		int unsignedByte = 0;
		if (lvalue < 0L) {
			unsignedByte = 0x40; lvalue ^= -1L; 
		}
	    unsignedByte |= (byte) ((int) lvalue & 0x3F);
	    lvalue >>>= 6;
	    while(lvalue != 0L) {
	    	unsignedByte |= 0x80;
	    	writeByte((byte) unsignedByte);
	    	unsignedByte = (int) lvalue & 0x7F;
	    	lvalue >>>= 7;
	    }
	    writeByte((byte) unsignedByte);
	}
	
	public static int estimateVLongSize(long lvalue) {
		int bytes = 0;
	    lvalue >>>= 6;
	    while(lvalue != 0L) {
	    	bytes++;
	    	lvalue >>>= 7;
	    }
	    bytes++;
	    return bytes;
	}

	public void writeString(String str) {
		writeVLong(str.length());
		writeUtf8(str, 0, str.length());
	}
	
	public void writeUtf8Char(char value) {
		
		int ch = value;
		
		if (ch <= 0x007F) {
			writeByte((byte) ch);
		}
		else if (ch <= 0x07FF) {
			writeByte((byte)(0xC0 | ch >> 6 & 0x1F));
			writeByte((byte)(0x80 | ch & 0x3F));
		}
		else if (ch >= 0xd800 && ch <= 0xdfff) {
			writeByte((byte) 0x3F);
		}
		else {
			writeByte((byte)(0xE0 | ch >> 12 & 0x0F));;
			writeByte((byte)(0x80 | ch >> 6 & 0x3F));
			writeByte((byte)(0x80 | ch & 0x3F));
		}
		
	}
	
	public static int estimateUtf8CharSize(char value) {
		
		int ch = value;
		
		if (ch <= 0x007F) {
			return 1;
		}
		else if (ch <= 0x07FF) {
			return 2;
		}
		else if (ch >= 0xd800 && ch <= 0xdfff) {
			return 1;
		}
		else {
			return 3;
		}
		
	}	
	
	public static int estimateUtf8Size(CharSequence value, int offset, int length) {
		Requires.nonNull(value, "value");
		Requires.positive(offset, "offset");
		Requires.positive(length, "length");

		int bytes = 0;
		for (int i = 0; i != length; ++i) {
			bytes += estimateUtf8CharSize(value.charAt(i + offset));
		}
		
		return bytes;
	}
	
	public static int estimateStringSize(String str) {
		return estimateVLongSize(str.length()) + estimateUtf8Size(str, 0, str.length());
	}
	
	public void writeUtf8(CharSequence value, int offset, int length) {
		
		Requires.nonNull(value, "value");
		Requires.positive(offset, "offset");
		Requires.positive(length, "length");
		
		for (int i = 0; i != length; ++i) {
			
			writeUtf8Char(value.charAt(i + offset));
			
		}
	}
	
	public void writeBytes(byte[] bytes) {
		writeBytes(bytes, 0, bytes.length);
	}
	
	public void writeBytes(byte[] bytes, int offset, int length) {
		Requires.nonNull(bytes, "bytes");
		Requires.positive(offset, "offset");
		Requires.positive(length, "length");
		
		int lenSize = estimateVLongSize(length);
		ensureCanGrow(lenSize + length);
		writeVLong(length);
		Unsafe.INSTANCE.copyMemory(bytes, Unsafe.BYTEARRAY_BASEOFFSET + offset * Unsafe.BYTEARRAY_INDEXSCALE, null, address + position, length);
		position += bytes.length;
	}
    
	public void writeInnerRef(int ref) {
		InnerRef innerRef = paging.getInnerRef();
		ensureCanGrow(innerRef.size());
		innerRef.writeInnerRef(address + position, ref);
		position += innerRef.size();
	}

	public void writePageNum(long page) {
		PageNum pageNum = paging.getPageNum();
		ensureCanGrow(pageNum.size());
		pageNum.writePageNum(address + position, page);
		position += pageNum.size();
	}

	public IntegerCas writeIntegerCas() {
    	ensureCanGrow(4);
    	IntegerCas result = new IntegerCas(address, position);
    	position += 4;
    	return result;
	}
	
	public LongCas writeLongCas() {
    	ensureCanGrow(8);
    	LongCas result = new LongCas(address, position);
    	position += 8;
    	return result;
	}
	
	public InnerRefCas writeInnerRefCas() {
		InnerRef innerRef = paging.getInnerRef();
    	ensureCanGrow(innerRef.size());
    	InnerRefCas result = new InnerRefCas(address, position, innerRef);
    	position += innerRef.size();
    	return result;
	}

	public PageNumCas writePageNumCas() {
		PageNum pageNum = paging.getPageNum();
    	ensureCanGrow(pageNum.size());
    	PageNumCas result = new PageNumCas(address, position, pageNum);
    	position += pageNum.size();
    	return result;
	}
	
}
