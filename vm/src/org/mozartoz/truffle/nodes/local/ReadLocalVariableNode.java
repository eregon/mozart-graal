package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadLocalVariableNode extends OzNode implements FrameSlotNode {

	@Child ReadFrameSlotNode readFrameSlotNode;

	public ReadLocalVariableNode(FrameSlot slot) {
		this.readFrameSlotNode = ReadFrameSlotNodeGen.create(slot);
	}

	public FrameSlot getSlot() {
		return readFrameSlotNode.getSlot();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return readFrameSlotNode.executeRead(frame);
	}

	@Override
	public String toString() {
		return super.toString() + " " + getSlot().getIdentifier();
	}

}
