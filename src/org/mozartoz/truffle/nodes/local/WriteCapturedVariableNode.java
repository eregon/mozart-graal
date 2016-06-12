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

	public FrameSlot getSlot() {
		return writeFrameSlotNode.getSlot();
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public void write(VirtualFrame topFrame, Object value) {
		Frame parentFrame = OzArguments.getParentFrame(topFrame, depth);
		writeFrameSlotNode.executeWrite(parentFrame, value);
	}

}
