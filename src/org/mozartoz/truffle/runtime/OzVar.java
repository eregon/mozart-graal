package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public class OzVar {
	private @CompilationFinal Object value = null;

	public boolean isBound() {
		return value != null;
	}

	public Object getValue() {
		return value;
	}

	public Object getBoundValue() {
		final Object value = this.value;
		if (!isBound()) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			throw new RuntimeException("unbound var");
		}
		return value;
	}

	public void bind(Object value) {
		assert !isBound();
		assert !(value instanceof OzVar);
		this.value = value;
	}

	@Override
	public String toString() {
		if (isBound()) {
			return "<" + value.toString() + ">";
		} else {
			return "_";
		}
	}

}
