package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class InitializeTmpNode extends OzNode implements FrameSlotNode {

	@Child WriteFrameSlotNode writeFrameSlotNode;
	@Child OzNode valueNode;

	public InitializeTmpNode(FrameSlot slot, OzNode valueNode) {
		this.writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
		this.valueNode = DerefIfBoundNode.create(valueNode);
	}

	public FrameSlot getSlot() {
		return writeFrameSlotNode.getSlot();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object value = valueNode.execute(frame);
		writeFrameSlotNode.executeWrite(frame, value);
		return value;
	}

}
