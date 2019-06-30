package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.Node;

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
		return false;
	}

	public abstract Object label(Object receiver);

	public abstract Arity arity(Object receiver);

	public abstract Object arityList(Object receiver);

	public abstract Object read(Object receiver, Object feature, Node node);

}
