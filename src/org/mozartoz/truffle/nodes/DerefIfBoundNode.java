package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzFuture;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class DerefIfBoundNode extends OzNode {

	public static DerefIfBoundNode create() {
		return DerefIfBoundNodeGen.create(null);
	}

	public abstract Object executeDerefIfBound(Object value);

	@Specialization
	Object derefIfBound(OzVar var) {
		if (var.isBound()) {
			return var.getBoundValue(this);
		} else {
			return var;
		}
	}

	@Specialization
	Object derefIfBound(OzFuture future) {
		if (future.isBound()) {
			return future.getBoundValue(this);
		} else {
			return future;
		}
	}

	@Specialization(guards = "!isVariable(value)")
	Object derefIfBound(Object value) {
		return value;
	}

}
