package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import org.mozartoz.truffle.nodes.OzGuards;

@ExportLibrary(value = RecordLibrary.class, receiverType = String.class)
public final class AtomLibraries {

	@ExportMessage
	static boolean isRecord(String atom) {
		assert OzGuards.isInterned(atom);
		return true;
	}

	@ExportMessage
	static Object label(String atom) {
		return atom;
	}

	@ExportMessage
	static Arity arity(String atom) {
		return Arity.forLiteral(atom);
	}

	@ExportMessage
	static Object arityList(String atom) {
		return "nil";
	}

	@ExportMessage
	static Object read(String atom, Object feature, Node node) {
		throw Errors.noFieldError(node, atom, feature);
	}

}
