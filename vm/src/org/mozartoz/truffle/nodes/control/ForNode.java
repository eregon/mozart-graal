package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.call.CallableNode;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

public class ForNode extends OzNode {

	@Child OzNode fromNode;
	@Child OzNode toNode;
	@Child OzNode procNode;

	@Child DerefNode fromDerefNode = DerefNode.create();
	@Child DerefNode toDerefNode = DerefNode.create();

	@Child CallableNode callNode = CallNode.create(null, null);
	private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

	public ForNode(OzNode fromNode, OzNode toNode, OzNode procNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.procNode = procNode;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		long from = (long) fromDerefNode.executeDeref(fromNode.execute(frame));
		long to = (long) toDerefNode.executeDeref(toNode.execute(frame));

		int count = 0;
		if (CompilerDirectives.inInterpreter()) {
			count = (int) Math.min(to - from + 1, Integer.MAX_VALUE);
		}

		try {
			loopProfile.profileCounted(count);
			for (long i = from; loopProfile.inject(i <= to); i++) {
				OzProc proc = (OzProc) procNode.execute(frame);
				callNode.executeCall(frame, proc, new Object[] { i });
			}
		} finally {
			LoopNode.reportLoopCount(this, count);
		}

		return unit;
	}

}
