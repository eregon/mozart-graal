package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public class WriteCapturedVariableNode extends Node implements WriteNode {

	final int depth;

	@Child WriteFrameSlotNode writeFrameSlotNode;

	public WriteCapturedVariableNode(FrameSlot slot, int depth) {
		this.depth = depth;
		this.writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
	}

	@Override
	public Object write(VirtualFrame topFrame, Object value) {
		Frame parentFrame = OzArguments.getParentFrame(topFrame, depth);
		return writeFrameSlotNode.executeWrite(parentFrame, value);
	}

}
