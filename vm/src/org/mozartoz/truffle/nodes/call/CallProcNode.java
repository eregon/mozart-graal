package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;

@NodeChildren({ @NodeChild("proc"), @NodeChild("arguments") })
public abstract class CallProcNode extends OzNode {

	/** Must only be used by CallNode */
	public static CallProcNode create() {
		return CallProcNodeGen.create(null, null);
	}

	abstract Object executeCall(VirtualFrame frame, OzProc proc, Object[] arguments);

	@Specialization(guards = "proc == cachedProc", limit = "1")
	protected Object callProcIdentity(VirtualFrame frame, OzProc proc, Object[] arguments,
			@Cached("proc") OzProc cachedProc,
			@Cached("create(cachedProc.callTarget)") DirectCallNode callNode) {
		return callNode.call(frame, OzArguments.pack(cachedProc.declarationFrame, arguments));
	}

	@Specialization(guards = "proc.callTarget == cachedCallTarget", contains = "callProcIdentity")
	protected Object callDirect(VirtualFrame frame, OzProc proc, Object[] arguments,
			@Cached("proc.callTarget") RootCallTarget cachedCallTarget,
			@Cached("create(cachedCallTarget)") DirectCallNode callNode) {
		return callNode.call(frame, OzArguments.pack(proc.declarationFrame, arguments));
	}

	@Specialization
	protected Object callIndirect(VirtualFrame frame, OzProc proc, Object[] arguments,
			@Cached("create()") IndirectCallNode callNode) {
		return callNode.call(frame, proc.callTarget, OzArguments.pack(proc.declarationFrame, arguments));
	}

}
