package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChildren({ @NodeChild("receiver"), @NodeChild("arguments") })
public abstract class CallNode extends OzNode {

	@CreateCast("receiver")
	protected OzNode derefReceiver(OzNode var) {
		return DerefNode.create(var);
	}

	public abstract Object executeCall(VirtualFrame frame, Object receiver, Object[] arguments);

	@Specialization
	protected Object callProc(VirtualFrame frame, OzProc proc, Object[] arguments,
			@Cached("create()") CallProcNode callProcNode) {
		return callProcNode.executeCall(frame, proc, arguments);
	}

	@Specialization
	protected Object callObject(VirtualFrame frame, OzObject object, Object[] arguments,
			@Cached("create()") CallMethodNode callMethodNode) {
		return callMethodNode.executeCall(frame, object, arguments);
	}

}
