package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadLocalVariableNode extends OzNode {

	@Child ReadFrameSlotNode readFrameSlotNode;

	public ReadLocalVariableNode(FrameSlot slot) {
		this.readFrameSlotNode = ReadFrameSlotNodeGen.create(slot);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		OzVar var = (OzVar) readFrameSlotNode.executeRead(frame);
		return var.getValue();
	}

}
