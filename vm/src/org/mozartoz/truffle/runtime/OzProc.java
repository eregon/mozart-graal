package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.MaterializedFrame;

public class OzProc {

	public @CompilationFinal RootCallTarget callTarget;
	public @CompilationFinal MaterializedFrame declarationFrame;
	public final int arity;

	public OzProc(RootCallTarget callTarget, MaterializedFrame declarationFrame, int arity) {
		this.callTarget = callTarget;
		this.declarationFrame = declarationFrame;
		this.arity = arity;
	}

	public Object rootCall(String identifier) {
		return wrap(identifier).call(OzArguments.pack(null, new Object[0]));
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
	private CallTarget wrap(String identifier) {
		OzNode callNode = CallNode.create(new LiteralNode(this), new LiteralNode(new Object[0]));
		OzRootNode rootNode = OzRootNode.createTopRootNode(identifier, callNode);
		return Truffle.getRuntime().createCallTarget(rootNode);
	}

}
