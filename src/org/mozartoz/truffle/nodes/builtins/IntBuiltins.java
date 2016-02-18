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

public abstract class IntBuiltins {

	private static final BigInteger ONE = BigInteger.valueOf(1);

	@Builtin(name = "+1")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class AddOneNode extends OzNode {

		@CreateCast("value")
		protected OzNode derefValue(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long addOne(long n) {
			return ExactMath.addExact(n, 1);
		}

		@Specialization
		protected BigInteger addOne(BigInteger n) {
			return n.add(ONE);
		}

	}

	@Builtin(name = "-1")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class SubOneNode extends OzNode {

		@CreateCast("value")
		protected OzNode derefValue(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long subOne(long n) {
			return ExactMath.subtractExact(n, 1);
		}

		@Specialization
		protected BigInteger subOne(BigInteger n) {
			return n.subtract(ONE);
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class DivNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization(rewriteOn = ArithmeticException.class)
		protected long div(long a, long b) {
			return a / b;
		}

		@Specialization
		protected BigInteger div(BigInteger a, BigInteger b) {
			return a.divide(b);
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class ModNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

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
