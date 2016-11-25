package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class TailCallException extends ControlFlowException {

	private static final long serialVersionUID = -6432472909387052509L;

	public final Object receiver;
	public @CompilationFinal(dimensions = 1) final Object[] arguments;

	public TailCallException(Object receiver, Object[] arguments) {
		this.receiver = receiver;
		this.arguments = arguments;
	}

}
