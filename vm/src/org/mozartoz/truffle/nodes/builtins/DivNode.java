package org.mozartoz.truffle.nodes.builtins;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class DivNode extends OzNode {

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