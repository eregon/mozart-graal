package org.mozartoz.truffle.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class ShowNode extends OzNode {

	@Specialization
	protected Object show(long value) {
		System.out.println(value);
		return unit;
	}

	@Specialization
	protected Object show(BigInteger value) {
		System.out.println(value);
		return unit;
	}

}
