package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadCapturedVariableNode extends OzNode {

	@Child ReadFrameSlotNode readFrameSlotNode;

	public ReadCapturedVariableNode(FrameSlot slot) {
		this.readFrameSlotNode = ReadFrameSlotNodeGen.create(slot);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		MaterializedFrame parentFrame = OzArguments.getParentFrame(frame);
		return readFrameSlotNode.executeRead(parentFrame);
	}

}
