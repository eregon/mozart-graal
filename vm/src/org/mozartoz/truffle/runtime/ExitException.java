package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.nodes.Node;

@SuppressWarnings("serial")
public final class ExitException extends RuntimeException implements TruffleException {

	private final Node location;
	private final int exitCode;

	public ExitException(Node location, int exitCode) {
		this.location = location;
		this.exitCode = exitCode;
	}

	@Override
	public Node getLocation() {
		return location;
	}

	@Override
	public boolean isExit() {
		return true;
	}

	@Override
	public int getExitStatus() {
		return exitCode;
	}

	@SuppressWarnings("sync-override")
	@Override
	public final Throwable fillInStackTrace() {
		return this;
	}

}
