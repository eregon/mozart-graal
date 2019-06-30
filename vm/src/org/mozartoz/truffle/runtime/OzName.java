package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

@ExportLibrary(RecordLibrary.class)
public class OzName extends OzValue implements Comparable<OzName> {

	private static long currentID = 0;

	private final long id;

	public OzName() {
		this.id = ++currentID;
	}

	@ExportMessage
	boolean isRecord() {
		return true;
	}

	@ExportMessage
	Object label() {
		return this;
	}

	@ExportMessage
	Arity arity() {
		return Arity.forLiteral(this);
	}

	@ExportMessage
	Object arityList() {
		return "nil";
	}

	@ExportMessage
	Object read(Object feature, Node node) {
		throw Errors.noFieldError(node, this, feature);
	}

	@Override
	public int compareTo(OzName other) {
		return Long.compare(this.id, other.id);
	}

	@Override
	public String toString() {
		return "<N>";
	}

}
