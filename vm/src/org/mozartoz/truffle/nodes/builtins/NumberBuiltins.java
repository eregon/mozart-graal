package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.NumberBuiltinsFactory.MulNodeFactory;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class NumberBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsNumberNode extends OzNode {

		@Specialization
		boolean isNumber(long value) {
			return true;
		}

		@Specialization
		boolean isNumber(double value) {
			return true;
		}

		@Specialization
		boolean isNumber(BigInteger value) {
			return true;
		}

		@Fallback
		boolean isNumber(Object value) {
			return false;
		}

	}

	@Builtin(name = "~", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("operand")
	public static abstract class NegNode extends OzNode {

		@Specialization
		long neg(long value) {
			return Math.subtractExact(0L, value);
		}

		@Specialization
		double neg(double value) {
			return -value;
		}

	}

	@Builtin(name = "pow", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class PowNode extends OzNode {

		@Specialization(guards = { "isInteger(base)", "cachedExponent <= 94", "exponent == cachedExponent" })
		@ExplodeLoop
		Object pow(Object base, long exponent,
				@Cached("exponent") long cachedExponent,
				@Cached MulNode mulNode) {
			Object result = 1L;
			while (cachedExponent > 0) {
				if (cachedExponent % 2 == 0) {
					base = mulNode.executeMul(base, base);
					cachedExponent /= 2;
				} else {
					result = mulNode.executeMul(base, result);
					cachedExponent -= 1;
				}
			}
			return result;
		}

		@Specialization(guards = { "isInteger(base)" })
		Object pow(Object base, long exponent,
				@Cached MulNode mulNode) {
			Object result = 1L;
			while (exponent > 0) {
				if (exponent % 2 == 0) {
					base = mulNode.executeMul(base, base);
					exponent /= 2;
				} else {
					result = mulNode.executeMul(base, result);
					exponent -= 1;
				}
			}
			return result;
		}

		@Specialization
		double pow(double left, double right) {
			return Math.pow(left, right);
		}
		
	}

	@Builtin(name = "abs", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("operand")
	public static abstract class AbsNode extends OzNode {

		@Specialization
		long abs(long operand,
				@Cached("createBinaryProfile()") ConditionProfile negative) {
			if (negative.profile(operand < 0)) {
				return -operand;
			} else {
				return operand;
			}
		}

		@Specialization
		double abs(double operand,
				@Cached("createBinaryProfile()") ConditionProfile negative) {
			if (negative.profile(operand < 0)) {
				return -operand;
			} else {
				return operand;
			}
		}

	}

	@Builtin(name = "+", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class AddNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		long add(long a, long b) {
			return Math.addExact(a, b);
		}

		@Specialization
		double add(double a, double b) {
			return a + b;
		}

		@TruffleBoundary
		@Specialization
		BigInteger add(BigInteger a, BigInteger b) {
			return a.add(b);
		}

	}

	@Builtin(name = "-", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class SubNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		long sub(long a, long b) {
			return Math.subtractExact(a, b);
		}

		@Specialization
		double sub(double a, double b) {
			return a - b;
		}

		@TruffleBoundary
		@Specialization
		BigInteger sub(BigInteger a, BigInteger b) {
			return a.subtract(b);
		}

	}

	@Builtin(name = "*", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class MulNode extends OzNode {

		public static MulNode create() {
			return MulNodeFactory.create(null, null);
		}

		public abstract Object executeMul(Object a, Object b);

		@Specialization(rewriteOn = ArithmeticException.class)
		long mul(long a, long b) {
			return Math.multiplyExact(a, b);
		}

		@TruffleBoundary
		@Specialization
		BigInteger mul(BigInteger a, BigInteger b) {
			return a.multiply(b);
		}

		@Specialization
		double mul(double a, double b) {
			return a * b;
		}

	}

}
