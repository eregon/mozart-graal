package org.mozartoz.truffle.runtime;

/**
 * Stacktraceless exception for triggering deoptimization manually.
 * SlowPathException cannot be used because it is a checked Exception.
 */
public final class DeoptimizingException extends RuntimeException {

	private static final long serialVersionUID = -304210520191451179L;

	public static final DeoptimizingException INSTANCE = new DeoptimizingException();

	private DeoptimizingException() {
	}

	@SuppressWarnings("sync-override")
	@Override
	public final Throwable fillInStackTrace() {
		return null;
	}

}
