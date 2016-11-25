package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzFailedValue;
import org.mozartoz.truffle.runtime.OzFuture;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class DerefNode extends OzNode {

	public static DerefNode create() {
		return DerefNodeGen.create(null);
	}

	public static DerefNode create(OzNode node) {
		assert !(node instanceof DerefNode);
		assert !(node instanceof DerefIfBoundNode);
		return DerefNodeGen.create(node);
	}

	public abstract OzNode getValue();

	public abstract Object executeDeref(Object value);

	@Specialization
	long deref(long value) {
		return value;
	}

	@Specialization(guards = {
			"value.getClass() == klass",
			"!isVariableClass(klass)", "!isFailedValueClass(klass)"
	})
	Object derefValueProfiled(Object value,
			@Cached("value.getClass()") Class<?> klass) {
		return klass.cast(value);
	}

	@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" }, contains = "derefValueProfiled")
	Object derefValue(Object value) {
		return value;
	}

	@Specialization
	Object deref(OzFailedValue failedValue) {
		throw new OzException(this, failedValue.getData());
	}

	@Specialization(guards = "isBound(var)")
	Object deref(OzVar var,
			@Cached("create()") DerefNode derefNode) {
		return derefNode.executeDeref(var.getBoundValue(this));
	}

	@Specialization(guards = "isBound(future)")
	Object deref(OzFuture future,
			@Cached("create()") DerefNode derefNode) {
		return derefNode.executeDeref(future.getBoundValue(this));
	}

	@Specialization(guards = "!isBound(var)")
	Object derefUnbound(OzVar var,
			@Cached("create()") DerefNode derefNode) {
		return derefNode.executeDeref(var.waitValue(this));
	}

	@Specialization(guards = "!isBound(future)")
	Object derefUnbound(OzFuture future,
			@Cached("create()") DerefNode derefNode) {
		return derefNode.executeDeref(future.waitValue(this));
	}

}
