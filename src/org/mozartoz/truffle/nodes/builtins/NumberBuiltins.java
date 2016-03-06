package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class NumberBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsNumberNode extends OzNode {

		@Specialization
		Object isNumber(Object value) {
			return unimplemented();
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

	}

	@GenerateNodeFactory
	@NodeChild("operand")
	public static abstract class AbsNode extends OzNode {

		@Specialization
		Object abs(Object operand) {
			return unimplemented();
		}

	}

	@Builtin(name = "+", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class AddNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long add(long a, long b) {
			return ExactMath.addExact(a, b);
		}

		@Specialization
		protected BigInteger add(BigInteger a, BigInteger b) {
			return a.add(b);
		}

	}

	@Builtin(name = "-", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class SubNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long sub(long a, long b) {
			return ExactMath.subtractExact(a, b);
		}

		@Specialization
		protected BigInteger sub(BigInteger a, BigInteger b) {
			return a.subtract(b);
		}

	}

	@Builtin(name = "*", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class MulNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long mul(long a, long b) {
			return ExactMath.multiplyExact(a, b);
		}

		@Specialization
		protected BigInteger mul(BigInteger a, BigInteger b) {
			return a.multiply(b);
		}

	}

}
