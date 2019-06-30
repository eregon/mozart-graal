package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(RecordLibrary.class)
public class Unit extends OzValue {

	public static final Unit INSTANCE = new Unit();

	private Unit() {
	}

	@ExportMessage
	boolean isLiteral() {
		return true;
	}

	@Override
	public String toString() {
		return "unit";
	}

}
