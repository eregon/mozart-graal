package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.mozartoz.truffle.nodes.OzGuards;

@ExportLibrary(value = RecordLibrary.class, receiverType = String.class)
public final class AtomLibraries {

	@ExportMessage
	static boolean isLiteral(String atom) {
		assert OzGuards.isInterned(atom);
		return true;
	}

}
