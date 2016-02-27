package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;

@NodeChildren({ @NodeChild("function") })
public abstract class CallProcNode extends OzNode {

	@Child ExecuteValuesNode executeArgumentsNode;

	public CallProcNode(OzNode[] argumentNodes) {
		this.executeArgumentsNode = new ExecuteValuesNode(argumentNodes);
	}

	@CreateCast("function")
	protected OzNode derefFunction(OzNode var) {
		return DerefNodeGen.create(var);
	}

	@Specialization(guards = "function.callTarget == cachedCallTarget")
	protected Object callDirect(VirtualFrame frame, OzProc function,
			@Cached("function.callTarget") CallTarget cachedCallTarget,
			@Cached("create(cachedCallTarget)") DirectCallNode callNode) {
		final Object[] arguments = executeArgumentsNode.executeValues(frame);
		return callNode.call(frame, OzArguments.pack(function.declarationFrame, arguments));
	}

	@Specialization
	protected Object callIndirect(VirtualFrame frame, OzProc function,
			@Cached("create()") IndirectCallNode callNode) {
		final Object[] arguments = executeArgumentsNode.executeValues(frame);
		return callNode.call(frame, function.callTarget, OzArguments.pack(function.declarationFrame, arguments));
	}

}
