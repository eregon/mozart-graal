package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

@ExportLibrary(RecordLibrary.class)
public class Unit extends OzValue {

	public static final Unit INSTANCE = new Unit();

	private Unit() {
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
	public String toString() {
		return "unit";
	}

}
