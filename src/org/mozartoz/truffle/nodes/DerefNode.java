package org.mozartoz.truffle.nodes;

import java.math.BigInteger;

import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class DerefNode extends OzNode {

	@Specialization
	public long deref(long value) {
		return value;
	}

	@Specialization
	public BigInteger deref(BigInteger value) {
		return value;
	}

	@Specialization
	public Object deref(OzVar var) {
		return var.getBoundValue();
	}

}
