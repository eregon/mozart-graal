package org.mozartoz.truffle.runtime;

import java.util.function.Consumer;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import org.mozartoz.truffle.nodes.DerefNode;

@ExportLibrary(RecordLibrary.class)
public class OzCons extends OzValue {

	public static final long HEAD = 1L;
	public static final long TAIL = 2L;
	private static final Object CONS_ARITY_LIST = Arity.CONS_ARITY.asOzList();

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

	public int length(DerefNode derefConsNode) {
		int length = 0;
		Object list = this;
		while (list instanceof OzCons) {
			OzCons cons = (OzCons) list;
			length++;
			list = derefConsNode.executeDeref(cons.getTail());
		}
		assert list == "nil";
		return length;
	}

	@ExportMessage
	boolean isRecord() {
		return true;
	}

	@ExportMessage
	Object label() {
		return "|";
	}

	@ExportMessage
	Arity arity() {
		return Arity.CONS_ARITY;
	}

	@ExportMessage
	Object arityList() {
		return CONS_ARITY_LIST;
	}

	@ExportMessage static class Read {
		@Specialization(guards = "feature == HEAD")
		static Object getHead(OzCons cons, long feature, Node node) {
			return cons.getHead();
		}

		@Specialization(guards = "feature == TAIL")
		static Object getTail(OzCons cons, long feature, Node node) {
			return cons.getTail();
		}

		@Fallback
		static Object getOther(OzCons cons, Object feature, Node node) {
			throw Errors.noFieldError(node, cons, feature);
		}
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException("OzCons has structural equality");
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
