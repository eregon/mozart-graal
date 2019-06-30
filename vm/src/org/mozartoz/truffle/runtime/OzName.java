package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(RecordLibrary.class)
public class OzName extends OzValue implements Comparable<OzName> {

	private static long currentID = 0;

	private final long id;

	public OzName() {
		this.id = ++currentID;
	}

	@ExportMessage
	boolean isLiteral() {
		return true;
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
