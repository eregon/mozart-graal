package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.CallProcNode;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

public class ForNode extends OzNode {

	public static FrameSlot createSlot(FrameDescriptor descriptor, String name) {
		return descriptor.addFrameSlot(new HiddenKey(name), FrameSlotKind.Long);
	}

	@Child OzNode fromNode;
	@Child OzNode toNode;

	private final FrameSlot I_SLOT;
	private final FrameSlot N_SLOT;

	@Child LoopNode loopNode;
	private final LoopConditionProfile loopProfile = LoopConditionProfile.createCountingProfile();

	public ForNode(OzNode fromNode, OzNode toNode, OzNode procNode, FrameSlot iSlot, FrameSlot nSlot) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.I_SLOT = iSlot;
		this.N_SLOT = nSlot;
		ForRepeatingNode repeatingNode = new ForRepeatingNode(procNode, I_SLOT, N_SLOT, loopProfile);
		this.loopNode = Truffle.getRuntime().createLoopNode(repeatingNode);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		long from = (long) fromNode.execute(frame);
		long to = (long) toNode.execute(frame);

		frame.setLong(I_SLOT, from);
		frame.setLong(N_SLOT, to);

		int count = 0;
		if (CompilerDirectives.inInterpreter()) {
			count = (int) Math.min(to - from + 1, Integer.MAX_VALUE);
		}
		loopProfile.profileCounted(count);
		loopNode.executeLoop(frame);
		return unit;
	}

	private static class ForRepeatingNode extends Node implements RepeatingNode {

		@Child OzNode procNode;
		@Child CallProcNode callProcNode = CallProcNode.create();

		private final FrameSlot I_SLOT;
		private final FrameSlot N_SLOT;
		private final LoopConditionProfile loopProfile;

		public ForRepeatingNode(OzNode procNode, FrameSlot iSlot, FrameSlot nSlot, LoopConditionProfile loopProfile) {
			this.procNode = procNode;
			this.I_SLOT = iSlot;
			this.N_SLOT = nSlot;
			this.loopProfile = loopProfile;
		}

		@Override
		public boolean executeRepeating(VirtualFrame frame) {
			long i = FrameUtil.getLongSafe(frame, I_SLOT);
			long n = FrameUtil.getLongSafe(frame, N_SLOT);
			if (loopProfile.inject(i <= n)) {
				OzProc proc = (OzProc) procNode.execute(frame);
				callProcNode.executeCall(frame, proc, new Object[] { i });
				frame.setLong(I_SLOT, i + 1);
				return true;
			} else {
				return false;
			}
		}

	}

}
