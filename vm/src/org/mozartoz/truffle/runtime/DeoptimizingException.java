package org.mozartoz.truffle.runtime;

/**
 * Stacktraceless exception for triggering deoptimization manually
 */
public final class DeoptimizingException extends RuntimeException {
	private static final long serialVersionUID = -304210520191451179L;
	public static final DeoptimizingException INSTANCE = new DeoptimizingException();

	private DeoptimizingException() {
		super(null, null);
	}

	@SuppressWarnings("sync-override")
	@Override
	public final Throwable fillInStackTrace() {
		return null;
	}

}
