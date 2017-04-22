package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

@ImportStatic(FrameSlotKind.class)
public abstract class WriteFrameSlotNode extends Node implements WriteNode, FrameSlotNode {

	public static WriteFrameSlotNode create(FrameSlot slot) {
		return WriteFrameSlotNodeGen.create(slot);
	}

	private final FrameSlot slot;

	public WriteFrameSlotNode(FrameSlot slot) {
		this.slot = slot;
	}

	public FrameSlot getSlot() {
		return slot;
	}

	public void write(VirtualFrame frame, Object value) {
		executeWrite(frame, value);
	}

	public abstract void executeWrite(Frame frame, Object value);

	@Specialization(guards = "isKind(frame, Long)")
	protected void writeLong(Frame frame, long value) {
		frame.setLong(slot, value);
	}

	@Specialization(replaces = "writeLong")
	protected void write(Frame frame, Object value) {
		if (slot.getKind() != FrameSlotKind.Object) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			slot.setKind(FrameSlotKind.Object);
		}
		frame.setObject(slot, value);
	}

	protected boolean isKind(Frame frame, FrameSlotKind kind) {
		if (slot.getKind() == kind) {
			return true;
		} else if (slot.getKind() == FrameSlotKind.Illegal) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			slot.setKind(kind);
			return true;
		} else {
			return false;
		}
	}

}
