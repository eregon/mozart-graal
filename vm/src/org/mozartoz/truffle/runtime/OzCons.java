package org.mozartoz.truffle.runtime;

import java.util.Iterator;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class OzCons implements Iterable<Object> {

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

	@TruffleBoundary
	@Override
	public Iterator<Object> iterator() {
		return new ConsIterator(this);
	}

	private static class ConsIterator implements Iterator<Object> {

		private OzCons current;

		public ConsIterator(OzCons cons) {
			current = cons;
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public Object next() {
			Object element = current.getHead();
			if (current.getTail() == "nil") {
				current = null;
			} else {
				current = (OzCons) current.getTail();
			}
			return element;
		}
	}

	@Override
	public String toString() {
		return head + "|" + tail;
	}

}
