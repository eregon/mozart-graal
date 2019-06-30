package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(value = RecordLibrary.class, receiverType = Boolean.class)
public final class BoolLibraries {

	@ExportMessage
	static boolean isLiteral(Boolean bool) {
		return true;
	}

}
