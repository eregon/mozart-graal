package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzVar;

public class OzGuards {

	public static boolean isLong(Object value) {
		return value instanceof Long;
	}

	public static boolean isNil(Object value) {
		return value == "nil";
	}

	public static boolean isCons(Object value) {
		return value instanceof OzCons;
	}

	public static boolean isVar(Object value) {
		return value instanceof OzVar;
	}

	public static boolean isBound(OzVar var) {
		return var.isBound();
	}

}
