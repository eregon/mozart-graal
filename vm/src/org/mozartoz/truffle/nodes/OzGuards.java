package org.mozartoz.truffle.nodes;

import java.math.BigInteger;

import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.object.DynamicObject;

public class OzGuards {

	public static boolean isLong(Object value) {
		return value instanceof Long;
	}

	public static boolean isBigInteger(Object value) {
		return value instanceof BigInteger;
	}

	public static boolean isNil(Object value) {
		return value == "nil";
	}

	public static boolean isCons(Object value) {
		return value instanceof OzCons;
	}

	public static boolean isRecord(Object value) {
		return value instanceof DynamicObject;
	}

	public static boolean isVar(Object value) {
		return value instanceof OzVar;
	}

	public static boolean isBound(OzVar var) {
		return var.isBound();
	}

}
