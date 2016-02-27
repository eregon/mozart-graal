package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class DerefNode extends OzNode {

	public static DerefNode create() {
		return DerefNodeGen.create(null);
	}

	public abstract Object executeDeref(Object value);

	@Specialization
	long deref(long value) {
		return value;
	}

	@Specialization(guards = "!isVar(value)")
	Object deref(Object value) {
		return value;
	}

	@Specialization
	Object deref(OzVar var) {
		return var.getBoundValue(this);
	}

}