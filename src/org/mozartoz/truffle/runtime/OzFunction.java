package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;

public class OzFunction {

	public final CallTarget callTarget;
	public final MaterializedFrame declarationFrame;

	public OzFunction(CallTarget callTarget, MaterializedFrame declarationFrame) {
		this.callTarget = callTarget;
		this.declarationFrame = declarationFrame;
	}

}
