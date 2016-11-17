package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.runtime.TailCallException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class TailCallCatcherNode extends CallableNode {

	@Child CallNode callNode;

	private final BranchProfile tailCallProfile = BranchProfile.create();

	public TailCallCatcherNode(CallNode callNode) {
		this.callNode = callNode;
	}

	public Object execute(VirtualFrame frame) {
		try {
			return callNode.execute(frame);
		} catch (TailCallException tailCall) {
			tailCallProfile.enter();
			return tailCallLoop(frame, tailCall);
		}
	}

	@Override
	public Object executeCall(VirtualFrame frame, Object receiver, Object[] arguments) {
		try {
			return callNode.executeCall(frame, receiver, arguments);
		} catch (TailCallException tailCall) {
			tailCallProfile.enter();
			receiver = null;
			arguments = null;
			return tailCallLoop(frame, tailCall);
		}
	}

	private Object tailCallLoop(VirtualFrame frame, TailCallException tailCall) {
		while (true) {
			try {
				return callNode.executeCall(frame, tailCall.receiver, tailCall.arguments);
			} catch (TailCallException exception) {
				tailCall = exception;
			}
		}
	}

}
