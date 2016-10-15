package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class IntBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsIntNode extends OzNode {

		@Specialization
		boolean isInt(long value) {
			return true;
		}

		@Specialization
		boolean isInt(BigInteger value) {
			return true;
		}

		@Specialization(guards = { "!isLong(value)", "!isBigInteger(value)" })
		boolean isInt(Object value) {
			return false;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class DivNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long div(long a, long b) {
			return a / b;
		}

		@Specialization
		protected BigInteger div(BigInteger a, BigInteger b) {
			return a.divide(b);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class ModNode extends OzNode {

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long mod(long a, long b) {
			return a % b;
		}

		@Specialization
		protected BigInteger mod(BigInteger a, BigInteger b) {
			return a.mod(b);
		}

	}

}
