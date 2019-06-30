package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

@ExportLibrary(value = RecordLibrary.class, receiverType = Boolean.class)
public final class BoolLibraries {

	@ExportMessage
	static boolean isRecord(Boolean bool) {
		return true;
	}

	@ExportMessage
	static Object label(Boolean bool) {
		return bool;
	}

	@ExportMessage
	static Arity arity(Boolean bool) {
		return Arity.forLiteral(bool);
	}

	@ExportMessage
	static Object arityList(Boolean bool) {
		return "nil";
	}

	@ExportMessage
	static Object read(Boolean bool, Object feature, Node node) {
		throw Errors.noFieldError(node, bool, feature);
	}

}
