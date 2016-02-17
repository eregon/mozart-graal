package org.mozartoz.truffle.nodes.builtins;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class NumberBuiltins {

	@Builtin(name = "+")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class AddNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long add(long a, long b) {
			return ExactMath.addExact(a, b);
		}

		@Specialization
		protected BigInteger add(BigInteger a, BigInteger b) {
			return a.add(b);
		}

	}

	@Builtin(name = "-")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class SubNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long sub(long a, long b) {
			return ExactMath.subtractExact(a, b);
		}

		@Specialization
		protected BigInteger sub(BigInteger a, BigInteger b) {
			return a.subtract(b);
		}

	}

	@Builtin(name = "*")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class MulNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

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
