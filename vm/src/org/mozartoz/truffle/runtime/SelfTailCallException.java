package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.nodes.ControlFlowException;

public class SelfTailCallException extends ControlFlowException {

	private static final long serialVersionUID = -6432472909387052509L;
	public static final SelfTailCallException INSTANCE = new SelfTailCallException();

	private SelfTailCallException() {
	}

}
