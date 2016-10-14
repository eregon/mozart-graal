package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class DerefIfBoundNode extends OzNode {

	public static DerefIfBoundNode create() {
		return DerefIfBoundNodeGen.create(null);
	}

	public static DerefIfBoundNode create(OzNode node) {
		assert !(node instanceof DerefIfBoundNode);
		assert !(node instanceof DerefNode);
		return DerefIfBoundNodeGen.create(node);
	}

	public abstract OzNode getValue();

	public abstract Object executeDerefIfBound(Object value);

	@Specialization(guards = "var.isBound()")
	Object derefBound(Variable var) {
		return var.getBoundValue(this);
	}

	@Specialization(guards = "!var.isBound()")
	Variable noDerefUnbound(Variable var) {
		return var;
	}

	@Specialization(guards = "!isVariable(value)")
	Object derefIfBound(Object value) {
		return value;
	}

}
