package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
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

	}

	@Builtin(name = "+", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class AddNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		long add(long a, long b) {
			return ExactMath.addExact(a, b);
		}

		@Specialization
		double add(double a, double b) {
			return a + b;
		}

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
			return ExactMath.subtractExact(a, b);
		}

		@Specialization
		double sub(double a, double b) {
			return a - b;
		}

		@Specialization
		BigInteger sub(BigInteger a, BigInteger b) {
			return a.subtract(b);
		}

	}

	@Builtin(name = "*", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class MulNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		long mul(long a, long b) {
			return ExactMath.multiplyExact(a, b);
		}

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
