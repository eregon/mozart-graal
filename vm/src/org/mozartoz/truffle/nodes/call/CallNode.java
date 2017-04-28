package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChildren({
		@NodeChild(value = "receiver", type = OzNode.class),
		@NodeChild(value = "arguments", type = OzNode.class)
})
public abstract class CallNode extends CallableNode {

	@CreateCast("receiver")
	protected OzNode derefReceiver(OzNode var) {
		return DerefNode.create(var);
	}

	public static CallableNode create() {
		return create(null, null);
	}

	public static CallableNode create(OzNode receiver, OzNode arguments) {
		if (Options.TAIL_CALLS) {
			// Always create a TailCallCatcherNode to ensure a TailCallException
			// never goes back further than the current call.
			return new TailCallCatcherNode(CallNodeGen.create(receiver, arguments));
		} else {
			return CallNodeGen.create(receiver, arguments);
		}
	}

	public abstract Object executeCall(VirtualFrame frame, Object receiver, Object[] arguments);

	@Specialization
	protected Object callProc(OzProc proc, Object[] arguments,
			@Cached("create()") CallProcNode callProcNode) {
		return callProcNode.executeCall(proc, arguments);
	}

	@Specialization
	protected Object callObject(OzObject object, Object[] arguments,
			@Cached("create()") CallMethodNode callMethodNode) {
		return callMethodNode.executeCall(object, arguments);
	}

	@Specialization
	protected Object callOther(VirtualFrame frame, Object object, Object[] arguments) {
		throw kernelError("type", unit, new OzCons(object, "nil"), "Callable", 1L, "nil");
	}

}
