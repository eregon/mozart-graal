package org.mozartoz.truffle.nodes.builtins;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class SubNode extends OzNode {

	@Specialization(rewriteOn = ArithmeticException.class)
	protected long sub(long a, long b) {
		return ExactMath.subtractExact(a, b);
	}

	@Specialization
	protected BigInteger sub(BigInteger a, BigInteger b) {
		return a.subtract(b);
	}

}
