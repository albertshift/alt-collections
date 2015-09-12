package alt.collections.util;

/**
 * 
 * @author Albert Shift
 *
 */
public class Unsafe {

    public static final sun.misc.Unsafe INSTANCE;

    public static final long BYTEARRAY_BASEOFFSET;
    public static final long BYTEARRAY_INDEXSCALE;
    
    static {
        try {
            INSTANCE = getUnsafe();
            BYTEARRAY_BASEOFFSET = INSTANCE.arrayBaseOffset(byte[].class);
            BYTEARRAY_INDEXSCALE = INSTANCE.arrayIndexScale(byte[].class);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private final static sun.misc.Unsafe getUnsafe() {
        try {
            return sun.misc.Unsafe.getUnsafe();
        } catch (SecurityException tryReflectionInstead) {}
        try {
            return java.security.AccessController.doPrivileged
            (new java.security.PrivilegedExceptionAction<sun.misc.Unsafe>() {
                public sun.misc.Unsafe run() throws Exception {
                    Class<sun.misc.Unsafe> k = sun.misc.Unsafe.class;
                    for (java.lang.reflect.Field f : k.getDeclaredFields()) {
                        f.setAccessible(true);
                        Object x = f.get(null);
                        if (k.isInstance(x)) {
                            return k.cast(x);
                        }
                    }
                    throw new NoSuchFieldError("the Unsafe");
                }});
        } catch (java.security.PrivilegedActionException e) {
            throw new RuntimeException("Could not initialize intrinsics",
                                       e.getCause());
        }
    }
    
}