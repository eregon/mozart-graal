package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class DerefIfBoundNode extends OzNode {

	@Specialization
	public Object derefIfBound(OzVar var) {
		if (var.isBound()) {
			return var.getBoundValue();
		} else {
			return var;
		}
	}

	@Specialization(guards = "!isVar(value)")
	public Object derefIfBound(Object value) {
		return value;
	}

}
