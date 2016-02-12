package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.builtins.RaiseErrorNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public class OzVar {
	private @CompilationFinal Object value = null;

	public boolean isBound() {
		return value != null;
	}

	public Object getBoundValue() {
		final Object value = this.value;
		if (!isBound()) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			RaiseErrorNode.showUserBacktrace(null);
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
