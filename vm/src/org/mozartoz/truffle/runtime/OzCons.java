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

	public void forEach(DerefNode derefConsNode, Consumer<Object> block) {
		Object list = this;
		while (list instanceof OzCons) {
			OzCons cons = (OzCons) list;
			block.accept(cons.getHead());
			list = derefConsNode.executeDeref(cons.getTail());
		}
		assert list == "nil";
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException("OzCons has structural equality");
	}

	@Override
	public String toString() {
		return head + "|" + tail;
	}

}
