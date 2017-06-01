package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.nodes.Node;

public abstract class ReadFrameSlotNode extends Node implements FrameSlotNode {

	private final FrameSlot slot;

	public ReadFrameSlotNode(FrameSlot slot) {
		this.slot = slot;
	}

	public FrameSlot getSlot() {
		return slot;
	}

	public abstract Object executeRead(Frame frame);

	@Specialization(rewriteOn = FrameSlotTypeException.class)
	protected long readLong(Frame frame) throws FrameSlotTypeException {
		return frame.getLong(slot);
	}

	@Specialization(rewriteOn = FrameSlotTypeException.class)
	protected double readDouble(Frame frame) throws FrameSlotTypeException {
		return frame.getDouble(slot);
	}

	@Specialization(rewriteOn = FrameSlotTypeException.class)
	protected boolean readBoolean(Frame frame) throws FrameSlotTypeException {
		return frame.getBoolean(slot);
	}

	@Specialization(rewriteOn = FrameSlotTypeException.class)
	protected Object readObject(Frame frame) throws FrameSlotTypeException {
		return frame.getObject(slot);
	}

	@Specialization(replaces = { "readLong", "readDouble", "readBoolean", "readObject" })
	protected Object read(Frame frame) {
		return frame.getValue(slot);
	}

	@Override
	public String toString() {
		return super.toString() + " " + slot.getIdentifier();
	}

}
