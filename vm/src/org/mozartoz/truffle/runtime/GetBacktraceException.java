package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.nodes.Node;

@SuppressWarnings("serial")
public final class GetBacktraceException extends RuntimeException implements TruffleException {

	private final Node currentNode;

	public GetBacktraceException(Node currentNode) {
		this.currentNode = currentNode;
	}

	@Override
	public Node getLocation() {
		return currentNode;
	}

	@SuppressWarnings("sync-override")
	@Override
	public final Throwable fillInStackTrace() {
		return this;
	}

}
