package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.SourceSection;

public abstract class Variable {

	private @CompilationFinal Object value = null;
	private boolean needed = false;

	/** A circular list of linked Variable */
	private Variable next = this;

	protected SourceSection declaration;

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

	public void link(Variable other) {
		assert !isBound();
		assert !other.isBound();

		// Link both circular lists
		Variable oldNext = this.next;
		this.next = other.next;
		other.next = oldNext;
	}

	public boolean isLinkedTo(Variable other) {
		Variable var = this;
		do {
			if (var == other) {
				return true;
			}
			var = var.next;
		} while (var != this);
		return false;
	}

	public Variable getNext() {
		return next;
	}

	protected void setValue(Object value, Variable from) {
		assert !isBound();
		assert !(value instanceof Variable);
		this.value = value;
	}

	public void bind(Object value) {
		setValue(value, this);

		Variable var = next;
		while (var != this) {
			var.setValue(value, this);
			var = var.next;
		}
	}

	public boolean isNeeded() {
		Variable var = this;
		do {
			if (var.needed) {
				return true;
			}
			var = var.next;
		} while (var != this);
		return false;
	}

	public void makeNeeded() {
		this.needed = true;
	}

	@TruffleBoundary
	public Object waitValue(OzNode currentNode) {
		makeNeeded();
		return waitValueQuiet(currentNode);
	}

	public Object waitValueQuiet(OzNode currentNode) {
		assert !isBound();
		while (!isBound()) {
			OzThread.getCurrent().yield();
		}
		return getBoundValue(currentNode);
	}

	@Override
	public abstract String toString();

}
