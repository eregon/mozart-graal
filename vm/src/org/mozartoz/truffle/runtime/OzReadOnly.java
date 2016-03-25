package org.mozartoz.truffle.runtime;

public class OzReadOnly extends Variable {

	public OzReadOnly(Variable variable) {
		link(variable);
	}

	@Override
	public void bind(Object value) {
		throw new UnsupportedOperationException();
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
