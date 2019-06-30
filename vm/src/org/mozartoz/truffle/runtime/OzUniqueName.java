package org.mozartoz.truffle.runtime;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

@ExportLibrary(RecordLibrary.class)
public class OzUniqueName extends OzValue implements Comparable<OzUniqueName> {

	private static final Map<String, OzUniqueName> TABLE = new HashMap<>();

	private String name;

	@TruffleBoundary
	public static OzUniqueName get(String name) {
		return TABLE.computeIfAbsent(name, OzUniqueName::new);
	}

	private OzUniqueName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
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
	public int compareTo(OzUniqueName other) {
		return name.compareTo(other.name);
	}

	@Override
	public String toString() {
		return "<N: " + name + ">";
	}

}
