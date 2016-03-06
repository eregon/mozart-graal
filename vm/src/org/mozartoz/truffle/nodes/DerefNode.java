package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzFailedValue;
import org.mozartoz.truffle.runtime.OzFuture;
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

	@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" })
	Object deref(Object value) {
		return value;
	}

	@Specialization
	Object deref(OzFailedValue failedValue) {
		throw new OzException(this, failedValue.getData());
	}

	@Specialization
	Object deref(OzVar var) {
		return check(var.getBoundValue(this));
	}

	@Specialization
	Object deref(OzFuture future) {
		return check(future.getBoundValue(this));
	}

	private Object check(Object value) {
		if (value instanceof OzFailedValue) {
			throw new OzException(this, ((OzFailedValue) value).getData());
		}
		return value;
	}

}
