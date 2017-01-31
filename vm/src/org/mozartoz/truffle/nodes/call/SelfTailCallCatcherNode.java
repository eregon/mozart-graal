package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.SelfTailCallException;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

public class SelfTailCallCatcherNode extends OzNode {

	OzNode body; // For serialization compliance
	@Child LoopNode loopNode;

	public SelfTailCallCatcherNode(OzNode body) {
		this.body = body;
		this.loopNode = Truffle.getRuntime().createLoopNode(new SelfTailCallLoopNode(body));
	}

	public static OzNode create(OzNode body) {
		if (!Options.SELF_TAIL_CALLS_OSR) {
			return new SelfTailCallCatcherNoOSRNode(body);
		}
		return new SelfTailCallCatcherNode(body);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		loopNode.executeLoop(frame);
		return unit;
	}

	private static class SelfTailCallLoopNode extends OzNode implements RepeatingNode {

		@Child OzNode body;

		final BranchProfile returnProfile = BranchProfile.create();
		final BranchProfile tailCallProfile = BranchProfile.create();
		final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

		public SelfTailCallLoopNode(OzNode body) {
			this.body = body;
		}

		@Override
		public boolean executeRepeating(VirtualFrame frame) {
			return loopProfile.profile(loopBody(frame));
		}

		private boolean loopBody(VirtualFrame frame) {
			try {
				body.execute(frame);
				returnProfile.enter();
				return false;
			} catch (SelfTailCallException e) {
				tailCallProfile.enter();
				return true;
			}
		}

		@Override
		public Object execute(VirtualFrame frame) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return OzBacktrace.formatNode(this);
		}

	}

	public static class SelfTailCallCatcherNoOSRNode extends OzNode {

		@Child OzNode body;

		final BranchProfile returnProfile = BranchProfile.create();
		final BranchProfile tailCallProfile = BranchProfile.create();
		final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

		public SelfTailCallCatcherNoOSRNode(OzNode body) {
			this.body = body;
		}

		@Override
		public Object execute(VirtualFrame frame) {
			while (loopProfile.profile(loopBody(frame))) {
			}
			return unit;
		}

		private boolean loopBody(VirtualFrame frame) {
			try {
				body.execute(frame);
				returnProfile.enter();
				return false;
			} catch (SelfTailCallException e) {
				tailCallProfile.enter();
				return true;
			}
		}

	}

}
