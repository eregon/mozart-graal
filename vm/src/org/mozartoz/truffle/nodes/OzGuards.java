package org.mozartoz.truffle.nodes;

import java.math.BigInteger;

import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzFailedValue;
import org.mozartoz.truffle.runtime.OzFuture;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzPort;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Unit;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.object.DynamicObject;

public class OzGuards {

	// Type guards

	public static boolean isLong(Object value) {
		return value instanceof Long;
	}

	public static boolean isBigInteger(Object value) {
		return value instanceof BigInteger;
	}

	public static boolean isFloat(Object value) {
		return value instanceof Double;
	}

	public static boolean isNil(Object value) {
		return value == "nil";
	}

	public static boolean isAtom(Object value) {
		assert !(value instanceof String) || isInterned((String) value);
		return value instanceof String;
	}

	public static boolean isBool(Object value) {
		return value instanceof Boolean;
	}

	public static boolean isUnit(Object value) {
		return value instanceof Unit;
	}

	public static boolean isName(Object value) {
		return value instanceof OzName;
	}

	public static boolean isNameLike(Object value) {
		return isBool(value) || isUnit(value) || isName(value) || isUniqueName(value);
	}

	public static boolean isUniqueName(Object value) {
		return value instanceof OzUniqueName;
	}

	public static boolean isLiteral(Object value) {
		return isAtom(value) || isNameLike(value);
	}

	public static boolean isFeature(Object value) {
		return isLong(value) || isBigInteger(value) || isLiteral(value);
	}

	public static boolean isCons(Object value) {
		return value instanceof OzCons;
	}

	public static boolean isRecord(Object value) {
		return value instanceof DynamicObject;
	}

	public static boolean isProc(Object value) {
		return value instanceof OzProc;
	}

	public static boolean isPort(Object value) {
		return value instanceof OzPort;
	}

	public static boolean isFailedValue(Object value) {
		return value instanceof OzFailedValue;
	}

	public static boolean isVar(Object value) {
		return value instanceof OzVar;
	}

	public static boolean isFuture(Object value) {
		return value instanceof OzFuture;
	}

	public static boolean isVariable(Object value) {
		return value instanceof Variable;
	}

	// Guards on specific types

	public static boolean isInt(long value) {
		return ((int) value) == value;
	}

	public static boolean isInterned(String str) {
		return str.intern() == str;
	}

	public static boolean isBound(Variable variable) {
		return variable.isBound();
	}

}
