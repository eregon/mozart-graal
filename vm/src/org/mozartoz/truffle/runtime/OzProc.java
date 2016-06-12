package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.source.SourceSection;

public class OzProc {

	public @CompilationFinal RootCallTarget callTarget;
	public @CompilationFinal MaterializedFrame declarationFrame;
	public final int arity;

	public OzProc(RootCallTarget callTarget, MaterializedFrame declarationFrame, int arity) {
		this.callTarget = callTarget;
		this.declarationFrame = declarationFrame;
		this.arity = arity;
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
