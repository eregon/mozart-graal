package org.mozartoz.truffle.runtime;

public class ArrayUtils {
	public static Object[] unshift(Object value, Object[] array) {
		Object[] result = new Object[1 + array.length];
		result[0] = value;
		System.arraycopy(array, 0, result, 1, array.length);
		return result;
	}
}
