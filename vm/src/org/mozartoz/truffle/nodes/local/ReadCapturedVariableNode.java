package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.ReadFrameSlotNodeGen;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzVar;

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
		OzVar var = (OzVar) readFrameSlotNode.executeRead(parentFrame);
		return var.getValue();
	}

}
