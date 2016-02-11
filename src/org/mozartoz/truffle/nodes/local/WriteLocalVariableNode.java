package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public class WriteLocalVariableNode extends Node implements WriteNode {

	@Child WriteFrameSlotNode writeFrameSlotNode;

	public WriteLocalVariableNode(FrameSlot slot) {
		this.writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
	}

	@Override
	public Object executeWrite(VirtualFrame frame, Object value) {
		return writeFrameSlotNode.executeWrite(frame, value);
	}

}
