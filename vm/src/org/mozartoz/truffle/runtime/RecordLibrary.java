package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.Node;
import org.mozartoz.truffle.nodes.OzGuards;

// Possible to share behavior for multiple classes?

@GenerateLibrary
@GenerateLibrary.DefaultExport(AtomLibraries.class)
@GenerateLibrary.DefaultExport(BoolLibraries.class)
public abstract class RecordLibrary extends Library {

	public static final int LIMIT = 7; // DynamicObject, OzCons, String, boolean, Unit, OzName, OzUniqueName
	private static final LibraryFactory<RecordLibrary> FACTORY = LibraryFactory.resolve(RecordLibrary.class);

	public static RecordLibrary getUncached() {
		return FACTORY.getUncached();
	}

	public boolean isRecord(Object receiver) {
		return isLiteral(receiver);
	}

	public boolean isLiteral(Object receiver) {
		return false;
	}

	@Abstract(ifExported = "isRecord")
	public Object label(Object receiver) {
		assert OzGuards.isLiteral(receiver);
		return receiver;
	}

	@Abstract(ifExported = "isRecord")
	public Arity arity(Object receiver) {
		assert OzGuards.isLiteral(receiver);
		return Arity.forLiteral(receiver);
	}

	@Abstract(ifExported = "isRecord")
	public Object arityList(Object receiver) {
		assert OzGuards.isLiteral(receiver);
		return "nil";
	}

	@Abstract(ifExported = "isRecord")
	public Object read(Object receiver, Object feature, Node node) {
		assert OzGuards.isLiteral(receiver);
		throw Errors.noFieldError(node, receiver, feature);
	}

}
