package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;

@NodeChildren({ @NodeChild("proc"), @NodeChild("arguments") })
public abstract class CallProcNode extends OzNode {

	/** Must only be used by CallNode */
	public static CallProcNode create() {
		return CallProcNodeGen.create(null, null);
	}

	abstract Object executeCall(OzProc proc, Object[] arguments);

	@Specialization(guards = "proc == cachedProc", limit = "1")
	protected Object callProcIdentity(OzProc proc, Object[] arguments,
			@Cached("proc") OzProc cachedProc,
			@Cached("createDirectCallNode(cachedProc.callTarget)") DirectCallNode callNode) {
		return callNode.call(OzArguments.pack(cachedProc.declarationFrame, arguments));
	}

	@Specialization(guards = "proc.callTarget == cachedCallTarget", contains = "callProcIdentity")
	protected Object callDirect(OzProc proc, Object[] arguments,
			@Cached("proc.callTarget") RootCallTarget cachedCallTarget,
			@Cached("createDirectCallNode(cachedCallTarget)") DirectCallNode callNode) {
		return callNode.call(OzArguments.pack(proc.declarationFrame, arguments));
	}

	@Specialization(contains = "callDirect")
	protected Object callIndirect(OzProc proc, Object[] arguments,
			@Cached("create()") IndirectCallNode callNode) {
		return callNode.call(proc.callTarget, OzArguments.pack(proc.declarationFrame, arguments));
	}

	protected static DirectCallNode createDirectCallNode(RootCallTarget callTarget) {
		DirectCallNode callNode = DirectCallNode.create(callTarget);
		OzRootNode rootNode = (OzRootNode) callTarget.getRootNode();
		if (Options.SPLIT_BUILTINS && rootNode.isForceSplitting()) {
			boolean cloned = callNode.cloneCallTarget();
			callNode.forceInlining();
			assert OzLanguage.ON_GRAAL == cloned;
		}
		return callNode;
	}

}
