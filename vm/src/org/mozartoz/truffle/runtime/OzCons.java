package org.mozartoz.truffle.runtime;

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

	@Override
	public String toString() {
		return head.toString() + "|" + tail.toString();
	}

}
