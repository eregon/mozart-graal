package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.nodes.Node;

@ImportStatic(FrameSlotKind.class)
public abstract class ReadFrameSlotNode extends Node implements FrameSlotNode {

	private final FrameSlot slot;

	public ReadFrameSlotNode(FrameSlot slot) {
		this.slot = slot;
	}

	public FrameSlot getSlot() {
		return slot;
	}

	public abstract Object executeRead(Frame frame);

	@Specialization(guards = "isKind(frame, Long)")
	protected long readLong(Frame frame) {
		return FrameUtil.getLongSafe(frame, slot);
	}

	@Specialization(guards = "isKind(frame, Double)")
	protected double readDouble(Frame frame) {
		return FrameUtil.getDoubleSafe(frame, slot);
	}

	@Specialization(guards = "isKind(frame, Boolean)")
	protected boolean readBoolean(Frame frame) {
		return FrameUtil.getBooleanSafe(frame, slot);
	}

	@Specialization(guards = "isKind(frame, Object)")
	protected Object readObject(Frame frame) {
		return FrameUtil.getObjectSafe(frame, slot);
	}

	@Specialization(replaces = { "readLong", "readDouble", "readBoolean", "readObject" })
	protected Object read(Frame frame) {
		return frame.getValue(slot);
	}

	protected boolean isKind(Frame frame, FrameSlotKind kind) {
		return frame.getFrameDescriptor().getFrameSlotKind(slot) == kind;

	}

	@Override
	public String toString() {
		return super.toString() + " " + slot.getIdentifier();
	}

}
