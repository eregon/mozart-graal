package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.SelfTailCallException;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

public class SelfTailCallCatcherNode extends OzNode {

	// For serialization compliance
	final OzNode body;
	final FrameDescriptor frameDescriptor;

	@Child LoopNode loopNode;

	public SelfTailCallCatcherNode(OzNode body, FrameDescriptor frameDescriptor) {
		this.body = body;
		this.frameDescriptor = frameDescriptor;
		this.loopNode = Truffle.getRuntime().createLoopNode(new SelfTailCallLoopNode(body, frameDescriptor));
	}

	public static OzNode create(OzNode body, FrameDescriptor frameDescriptor) {
		if (!Options.SELF_TAIL_CALLS_OSR) {
			return new SelfTailCallCatcherNoOSRNode(body, frameDescriptor);
		}
		return new SelfTailCallCatcherNode(body, frameDescriptor);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		loopNode.executeLoop(frame);
		return unit;
	}

	private static class SelfTailCallLoopNode extends OzNode implements RepeatingNode {

		@Child OzNode body;

		final FrameDescriptor frameDescriptor;
		final BranchProfile returnProfile = BranchProfile.create();
		final BranchProfile tailCallProfile = BranchProfile.create();
		final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

		public SelfTailCallLoopNode(OzNode body, FrameDescriptor frameDescriptor) {
			this.frameDescriptor = frameDescriptor;
			this.body = body;
		}

		@Override
		public boolean executeRepeating(VirtualFrame frame) {
			return loopProfile.profile(loopBody(frame));
		}

		private boolean loopBody(VirtualFrame frame) {
			try {
				body.execute(Truffle.getRuntime().createVirtualFrame(frame.getArguments(), frameDescriptor));
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

		final FrameDescriptor frameDescriptor;
		final BranchProfile returnProfile = BranchProfile.create();
		final BranchProfile tailCallProfile = BranchProfile.create();
		final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

		public SelfTailCallCatcherNoOSRNode(OzNode body, FrameDescriptor frameDescriptor) {
			this.body = body;
			this.frameDescriptor = frameDescriptor;
		}

		@Override
		public Object execute(VirtualFrame frame) {
			while (loopProfile.profile(loopBody(frame))) {
			}
			return unit;
		}

		private boolean loopBody(VirtualFrame frame) {
			try {
				body.execute(Truffle.getRuntime().createVirtualFrame(frame.getArguments(), frameDescriptor));
				returnProfile.enter();
				return false;
			} catch (SelfTailCallException e) {
				tailCallProfile.enter();
				return true;
			}
		}

	}

}
