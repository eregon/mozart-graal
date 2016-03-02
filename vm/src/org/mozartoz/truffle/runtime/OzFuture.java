package org.mozartoz.truffle.runtime;

public class OzFuture extends Variable {

	@Override
	protected void setValue(Object value, Variable from) {
		assert from instanceof OzFuture;
		super.setValue(value, from);
	}

	@Override
	public String toString() {
		if (isBound()) {
			return "_<" + getBoundValue(null) + ">";
		} else {
			return "_<future>";
		}
	}

}
