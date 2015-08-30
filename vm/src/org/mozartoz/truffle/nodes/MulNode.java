package org.mozartoz.truffle.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class MulNode extends OzNode {

	@Specialization(rewriteOn = ArithmeticException.class)
	protected long mul(long a, long b) {
		return ExactMath.multiplyExact(a, b);
	}

	@Specialization
	protected BigInteger mul(BigInteger a, BigInteger b) {
		return a.multiply(b);
	}

}
