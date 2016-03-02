package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.source.SourceSection;

public class OzProc {

	public final RootCallTarget callTarget;
	public final MaterializedFrame declarationFrame;

	public OzProc(RootCallTarget callTarget, MaterializedFrame declarationFrame) {
		this.callTarget = callTarget;
		this.declarationFrame = declarationFrame;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof OzProc)) {
			return false;
		}
		OzProc proc = (OzProc) obj;
		return callTarget.getRootNode().getSourceSection() == proc.callTarget.getRootNode().getSourceSection();
	}

	@Override
	public String toString() {
		SourceSection sourceSection = callTarget.getRootNode().getSourceSection();
		return "<Proc@" + sourceSection.getShortDescription() + ">";
	}

}
