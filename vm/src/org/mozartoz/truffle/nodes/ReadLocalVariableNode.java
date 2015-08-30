package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadLocalVariableNode extends OzNode {

	@Child ReadFrameSlotNode readFrameSlotNode;

	public ReadLocalVariableNode(FrameSlot slot) {
		this.readFrameSlotNode = ReadFrameSlotNodeGen.create(slot);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return readFrameSlotNode.executeRead(frame);
	}

}
