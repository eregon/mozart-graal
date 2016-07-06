package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.source.SourceSection;

public class OzVar extends Variable {

	public OzVar() {
	}

	public OzVar(SourceSection declaration) {
		this.declaration = declaration;
	}

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
