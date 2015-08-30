package org.mozartoz.truffle.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class AddNode extends OzNode {

	@Specialization(rewriteOn = ArithmeticException.class)
	protected long add(long a, long b) {
		return ExactMath.addExact(a, b);
	}

	@Specialization
	protected BigInteger addOverflow(long a, long b) {
		return BigInteger.valueOf(a).add(BigInteger.valueOf(b));
	}

}
