package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class InitializeTmpNode extends OzNode implements FrameSlotNode {

	@Child WriteFrameSlotNode writeFrameSlotNode;
	@Child OzNode value;

	public InitializeTmpNode(FrameSlot slot, OzNode value) {
		this.writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
		this.value = value;
	}

	public FrameSlot getSlot() {
		return writeFrameSlotNode.getSlot();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		writeFrameSlotNode.executeWrite(frame, value.execute(frame));
		return unit;
	}

}
