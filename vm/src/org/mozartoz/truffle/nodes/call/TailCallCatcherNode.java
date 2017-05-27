package org.mozartoz.truffle.nodes.call;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.OzLanguage;
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
			this.loopNode = insert(TailCallLoopNode.createOptimizedLoopNode(frame.getFrameDescriptor()));
		}

		TailCallLoopNode tailCallLoop = (TailCallLoopNode) loopNode.getRepeatingNode();
		tailCallLoop.setTailCallException(frame, tailCall);
		if (Options.TAIL_CALLS_OSR) {
			loopNode.executeLoop(frame);
		} else {
			tailCallLoop.execute(frame);
		}
		return unit;
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

		private Object getReceiver(VirtualFrame frame) {
			Object receiver = FrameUtil.getObjectSafe(frame, receiverSlot);
			frame.setObject(receiverSlot, null); // Clear it to avoid leaking during the call
			return receiver;
		}

		private Object[] getArguments(VirtualFrame frame) {
			Object[] arguments = (Object[]) FrameUtil.getObjectSafe(frame, argumentsSlot);
			frame.setObject(argumentsSlot, null); // Clear it to avoid leaking during the call
			return arguments;
		}

		// normal
		@Override
		public Object execute(VirtualFrame frame) {
			while (loopProfile.profile(loopBody(frame))) {
			}
			return unit;
		}

		// OSR
		@Override
		public boolean executeRepeating(VirtualFrame frame) {
			return loopProfile.profile(loopBody(frame));
		}

		private boolean loopBody(VirtualFrame frame) {
			try {
				callNode.executeCall(frame, getReceiver(frame), getArguments(frame));
				normalCallProfile.enter();
				return false;
			} catch (TailCallException exception) {
				exceptionProfile.enter();
				setTailCallException(frame, exception);
				return true;
			}
		}

		@Override
		public String toString() {
			return OzBacktrace.formatNode(this);
		}

		static LoopNode createOptimizedLoopNode(FrameDescriptor frameDescriptor) {
			TailCallLoopNode repeatingNode = new TailCallLoopNode(frameDescriptor);
			LoopNode loopNode = Truffle.getRuntime().createLoopNode(repeatingNode);
			if (!OzLanguage.ON_GRAAL) {
				return loopNode;
			}
			try {
				Method createOSRLoop = loopNode.getClass().getMethod("createOSRLoop", new Class[] {
						RepeatingNode.class, int.class, int.class, FrameSlot[].class, FrameSlot[].class });
				FrameSlot[] slots = new FrameSlot[] { repeatingNode.receiverSlot, repeatingNode.argumentsSlot };
				return (LoopNode) createOSRLoop.invoke(null,
						repeatingNode,
						Options.TruffleOSRCompilationThreshold,
						Options.TruffleInvalidationReprofileCount,
						slots,
						slots);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new Error(e);
			}
		}

	}

}
