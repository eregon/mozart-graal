package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.source.SourceSection;

public class OzProc {

	public final CallTarget callTarget;
	public final MaterializedFrame declarationFrame;

	public OzProc(CallTarget callTarget, MaterializedFrame declarationFrame) {
		this.callTarget = callTarget;
		this.declarationFrame = declarationFrame;
	}

	@Override
	public String toString() {
		SourceSection sourceSection = ((RootCallTarget) callTarget).getRootNode().getSourceSection();
		return "<Proc@" + sourceSection.getShortDescription() + ">";
	}

}
