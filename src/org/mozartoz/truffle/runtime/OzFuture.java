package org.mozartoz.truffle.runtime;

public class OzFuture extends Variable {

	@Override
	public String toString() {
		if (isBound()) {
			return "_<" + getBoundValue(null) + ">";
		} else {
			return "_<future>";
		}
	}

}
