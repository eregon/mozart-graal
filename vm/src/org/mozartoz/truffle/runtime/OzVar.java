package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public class OzVar {

	private @CompilationFinal Object value = null;

	/** A circular list of linked OzVar */
	private OzVar next = this;

	public boolean isBound() {
		return value != null;
	}

	public Object getBoundValue(OzNode currentNode) {
		final Object value = this.value;
		if (!isBound()) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			throw new OzException(currentNode, "unbound var");
		}
		return value;
	}

	public void link(OzVar other) {
		assert !isBound();
		assert !other.isBound();

		// Link both circular lists
		OzVar oldNext = this.next;
		this.next = other.next;
		other.next = oldNext;
	}

	public void bind(Object value) {
		assert !isBound();
		assert !(value instanceof OzVar);
		this.value = value;

		if (!next.isBound()) { // back to the first bind in the circle
			next.bind(value);
		}
		// TODO: next = null; ?
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
