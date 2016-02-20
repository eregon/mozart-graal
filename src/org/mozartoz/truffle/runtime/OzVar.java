package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public class OzVar {

	private @CompilationFinal Object value = null;
	private boolean dead = false;

	public boolean isBound() {
		return value != null;
	}

	public Object getBoundValue(OzNode currentNode) {
		assert !dead;
		final Object value = this.value;
		if (!isBound()) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			throw new OzError(currentNode, "unbound var");
		}
		return value;
	}

	public void bind(Object value) {
		assert !dead;
		assert !isBound();
		assert !(value instanceof OzVar);
		this.value = value;
	}

	public void setDead() {
		this.dead = true;
	}

	@Override
	public String toString() {
		if (isBound()) {
			return value.toString();
		} else {
			return "_";
		}
	}

}
