package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.TailCallException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class TailCallCatcherNode extends OzNode {

	@Child CallNode callNode;

	private final BranchProfile tailCallProfile = BranchProfile.create();

	public TailCallCatcherNode(CallNode callNode) {
		this.callNode = callNode;
	}

	public Object execute(VirtualFrame frame) {
		try {
			return callNode.execute(frame);
		} catch (TailCallException firstException) {
			tailCallProfile.enter();
			TailCallException tailCall = firstException;
			while (true) {
				try {
					return callNode.executeCall(frame, tailCall.receiver, tailCall.arguments);
				} catch (TailCallException exception) {
					tailCall = exception;
				}
			}
		}
	}

}
