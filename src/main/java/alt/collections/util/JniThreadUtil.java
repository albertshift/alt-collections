package alt.collections.util;

/**
 * JNI Version of the ThreadUtil
 * 
 * @author Albert Shift
 *
 */

public class JniThreadUtil {

	static {
	    System.loadLibrary("threadutil");
	}
	   
	public static native void usleep(long microseconds);
	
}
