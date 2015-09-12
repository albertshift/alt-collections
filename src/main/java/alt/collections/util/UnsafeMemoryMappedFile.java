package alt.collections.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

import sun.nio.ch.FileChannelImpl;

/**
 * 
 * @author Albert Shift
 *
 */
public class UnsafeMemoryMappedFile {

	public static final int MAP_READONLY = 0;
	public static final int MAP_READWRITE = 1;
	public static final int MAP_PRIVATE = 2;
	  
    private static final Method map0 = getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
    private static final Method unmap0 = getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);

    private long address;
    private final long size;

    public UnsafeMemoryMappedFile(String fileName, MapFileMode mode, long size) throws Exception {
    	Requires.nonNull(fileName, "fileName");
    	Requires.positive(size, "size");
    	Requires.aligned(PageSize.UNSAFE_PAGESIZE, size, "size");
 
        RandomAccessFile raf = new RandomAccessFile(getFile(fileName), "rw");
        FileChannel ch = null;

        try {
            raf.setLength(size);
            ch = raf.getChannel();
            this.address = (Long) map0.invoke(ch, mode.getValue(), 0L, size);
            this.size = size;
        } finally {
            if (ch != null) {
                ch.close();
            }
            raf.close();
        }
    }

    public synchronized void close() {
        if (address != 0) {
            try {
                unmap0.invoke(null, address, size);
            } catch (Exception e) {
                // ignore
            }
            address = 0;
        }
    }

    public final long getAddress() {
        return address;
    }

    public final long getSize() {
        return size;
    }
    
    public static Method getMethod(Class<?> cls, String name, Class<?>... params) {
        try {
            Method m = cls.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
	public static File getFile(String fileName) {
		File file = new File(fileName);
		createParentDirectoriesIfNeeded(file);
		return file;
	}
    
    public static void createParentDirectoriesIfNeeded(File file) {
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
	}
	
}
