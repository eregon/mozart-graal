package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class WriteLocalVariableNode extends OzNode {

	@Child WriteFrameSlotNode writeFrameSlotNode;
	@Child OzNode value;

	public WriteLocalVariableNode(FrameSlot slot, OzNode value) {
		assert slot != null;
		this.writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
		this.value = value;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return writeFrameSlotNode.executeWrite(frame, value.execute(frame));
	}

}
