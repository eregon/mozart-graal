package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;

public class OzProc extends OzValue {

	public @CompilationFinal RootCallTarget callTarget;
	public @CompilationFinal MaterializedFrame declarationFrame;
	public final int arity;

	public OzProc(RootCallTarget callTarget, MaterializedFrame declarationFrame, int arity) {
		this.callTarget = callTarget;
		this.declarationFrame = declarationFrame;
		this.arity = arity;
	}

	@TruffleBoundary
	public Object rootCall(String identifier, Object... arguments) {
		final OzLanguage language = callTarget.getRootNode().getLanguage(OzLanguage.class);
		final CallTarget target = wrap(language, identifier, new LiteralNode(this), arguments);
		return target.call(OzArguments.pack(null, ArrayUtils.EMPTY));
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
		return "<Proc " + OzBacktrace.formatNode(callTarget.getRootNode()) + ">";
	}

	/** Wraps itself in a CallNode so it works well with TailCallException */
	public static CallTarget wrap(OzLanguage language, String identifier, OzNode procNode, Object[] arguments) {
		OzNode callNode = CallNode.create(procNode, new LiteralNode(arguments));
		return OzRootNode.createTopRootNode(language, identifier, callNode).toCallTarget();
	}

}
