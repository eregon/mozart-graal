package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.TailCallException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

public class TailCallCatcherNode extends CallableNode {

	@Child CallNode callNode;
	@Child LoopNode loopNode;

	private final BranchProfile normalCallProfile = BranchProfile.create();
	private final BranchProfile tailCallProfile = BranchProfile.create();

	public TailCallCatcherNode(CallNode callNode) {
		this.callNode = callNode;
	}

	public Object execute(VirtualFrame frame) {
		try {
			callNode.execute(frame);
			normalCallProfile.enter();
			return unit;
		} catch (TailCallException tailCall) {
			tailCallProfile.enter();
			return tailCallLoop(frame, tailCall);
		}
	}

	@Override
	public Object executeCall(VirtualFrame frame, Object receiver, Object[] arguments) {
		try {
			callNode.executeCall(frame, receiver, arguments);
			normalCallProfile.enter();
			return unit;
		} catch (TailCallException tailCall) {
			tailCallProfile.enter();
			receiver = null;
			arguments = null;
			return tailCallLoop(frame, tailCall);
		}
	}

	private Object tailCallLoop(VirtualFrame frame, TailCallException tailCall) {
		if (loopNode == null) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			this.loopNode = insert(Truffle.getRuntime().createLoopNode(new TailCallLoopNode(frame.getFrameDescriptor())));
		}

		TailCallLoopNode tailCallLoop = (TailCallLoopNode) loopNode.getRepeatingNode();
		tailCallLoop.setTailCallException(frame, tailCall);
		loopNode.executeLoop(frame);
		return tailCallLoop.getResult(frame);
	}

	public static class TailCallLoopNode extends OzNode implements RepeatingNode {

		private final FrameSlot receiverSlot;
		private final FrameSlot argumentsSlot;
		private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();
		private final BranchProfile normalCallProfile = BranchProfile.create();
		private final BranchProfile exceptionProfile = BranchProfile.create();

		@Child CallNode callNode = CallNodeGen.create(null, null);

		public TailCallLoopNode(FrameDescriptor frameDescriptor) {
			this.receiverSlot = frameDescriptor.findOrAddFrameSlot("<OSRtailCallReceiver>", FrameSlotKind.Object);
			this.argumentsSlot = frameDescriptor.findOrAddFrameSlot("<OSRtailCallArgs>", FrameSlotKind.Object);
			loopProfile.profile(false);
		}

		public void setTailCallException(VirtualFrame frame, TailCallException tailCall) {
			frame.setObject(receiverSlot, tailCall.receiver);
			frame.setObject(argumentsSlot, tailCall.arguments);
		}

		public Object getResult(VirtualFrame frame) {
			frame.setObject(receiverSlot, null);
			frame.setObject(argumentsSlot, null);
			return unit;
		}

		@Override
		public boolean executeRepeating(VirtualFrame frame) {
			return loopProfile.profile(loopBody(frame));
		}

		private boolean loopBody(VirtualFrame frame) {
			Object receiver = FrameUtil.getObjectSafe(frame, receiverSlot);
			Object[] arguments = (Object[]) FrameUtil.getObjectSafe(frame, argumentsSlot);
			try {
				callNode.executeCall(frame, receiver, arguments);
				normalCallProfile.enter();
				return false;
			} catch (TailCallException exception) {
				exceptionProfile.enter();
				setTailCallException(frame, exception);
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

}
