package name.tbh.wowmon.common;


public class Util {

	public static boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static String nullIfEmpty(String s) {
		return isEmpty(s) ? null : s;
	}

}
