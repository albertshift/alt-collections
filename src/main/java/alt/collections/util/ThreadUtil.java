package alt.collections.util;

import java.lang.reflect.Method;
import java.util.Random;

public class ThreadUtil {

	private static final Random random = new Random();
	
	private static final Method usleepMethod;
	
	static {
		Method method = null;
		try {
			String jniClassName = ThreadUtil.class.getPackage().getName() + ".JniThreadUtil";
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl == null) {
				cl = ThreadUtil.class.getClassLoader();
			}
			Class<?> jniClass = cl.loadClass(jniClassName);
			
			boolean libraryLoaded = false;
			try {
				jniClass.newInstance();
				libraryLoaded = true;
			}
			catch(UnsatisfiedLinkError e) {
			}
			
			if (libraryLoaded) {
				for (Method m : jniClass.getDeclaredMethods()) {
					if (m.getName().equals("usleep")) {
						method = m;
						break;
					}
				}
			}
			
		}
		catch(Exception e) {
			method = null;
			e.printStackTrace();
		}
		usleepMethod = method;
	}
	
	public static boolean isJniLoaded() {
		return usleepMethod != null;
	}
	
	public static void loopSleep() {
		//usleep(128 + random.nextInt(128));
		//usleep(25);
		//try {
		//	Thread.sleep(0, 2500);
		//} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
	}
	
	public static void usleep(long microseconds) {
		if (usleepMethod != null) {
			try {
				usleepMethod.invoke(null, microseconds);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
}
