package org.mozartoz.truffle.runtime;

import java.util.function.Consumer;

import org.mozartoz.truffle.nodes.DerefNode;

public class OzCons {

	final Object head;
	final Object tail;

	public OzCons(Object head, Object tail) {
		this.head = head;
		this.tail = tail;
	}

	public Object getHead() {
		return head;
	}

	public Object getTail() {
		return tail;
	}

	public void forEach(DerefNode deref, Consumer<Object> block) {
		Object list = this;
		while (list instanceof OzCons) {
			OzCons cons = (OzCons) list;
			Object head = deref.executeDeref(cons.getHead());
			block.accept(head);
			list = deref.executeDeref(cons.getTail());
		}
		assert list == "nil";
	}

	@Override
	public String toString() {
		return head + "|" + tail;
	}

}
