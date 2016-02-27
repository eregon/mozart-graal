package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzNode;

@SuppressWarnings("serial")
public class OzException extends RuntimeException {

	private OzBacktrace backtrace;

	public OzException(OzNode currentNode, String message) {
		super(message);
		this.backtrace = OzBacktrace.capture(currentNode);
	}

	public OzBacktrace getBacktrace() {
		return backtrace;
	}

}