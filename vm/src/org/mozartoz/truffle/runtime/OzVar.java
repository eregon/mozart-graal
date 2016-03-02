package org.mozartoz.truffle.runtime;

public class OzVar extends Variable {

	public OzFuture findFuture() {
		Variable var = getNext();
		while (var != this) {
			if (var instanceof OzFuture) {
				return (OzFuture) var;
			}
			var = var.getNext();
		}
		return null;
	}

	@Override
	public String toString() {
		if (isBound()) {
			return "<" + getBoundValue(null) + ">";
		} else {
			return "_";
		}
	}

}
