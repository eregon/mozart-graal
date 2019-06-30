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

	public static boolean isBigIntegerClass(Class<?> klass) {
		return BigInteger.class.isAssignableFrom(klass);
	}

	public static boolean isInteger(Object value) {
		return value instanceof Long || value instanceof BigInteger;
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

	public static boolean isAtomClass(Class<?> klass) {
		return String.class.isAssignableFrom(klass);
	}

	public static boolean isBool(Object value) {
		return value instanceof Boolean;
	}

	public static boolean isUnit(Object value) {
		return value == Unit.INSTANCE;
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

	/** String, boolean, Unit, OzName, OzUniqueName */
	public static boolean isLiteral(Object value) {
		return isAtom(value) || isNameLike(value);
	}

	public static boolean isFeature(Object value) {
		return isLong(value) || isBigInteger(value) || isLiteral(value);
	}

	public static boolean isCons(Object value) {
		return value instanceof OzCons;
	}

	public static boolean isConsClass(Class<?> klass) {
		return OzCons.class.isAssignableFrom(klass);
	}

	public static boolean isRecord(Object value) {
		return value instanceof DynamicObject;
	}

	public static boolean isRecordClass(Class<?> klass) {
		return DynamicObject.class.isAssignableFrom(klass);
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

	public static boolean isFailedValueClass(Class<?> klass) {
		return klass == OzFailedValue.class;
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

	public static boolean isVariableClass(Class<?> klass) {
		return Variable.class.isAssignableFrom(klass);
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

	// Equality guards

	public static boolean hasValueEquality(Object value) {
		return isBool(value) || isLong(value) || isBigInteger(value) || isFloat(value);
	}

	public static boolean hasStructuralEquality(Object value) {
		return isCons(value) || isRecord(value) || isProc(value);
	}

	public static boolean hasReferenceEquality(Object value) {
		return !isBool(value) && !isLong(value) && !isBigInteger(value) && !isFloat(value)
				&& !isCons(value) && !isRecord(value) && !isProc(value);
	}

}
