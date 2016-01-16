package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class WriteLocalVariableNode extends OzNode {

	@Child ReadFrameSlotNode readFrameSlotNode;
	@Child OzNode valueNode;

	public WriteLocalVariableNode(FrameSlot slot, OzNode value) {
		assert slot != null;
		this.readFrameSlotNode = ReadFrameSlotNodeGen.create(slot);
		this.valueNode = value;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		OzVar var = (OzVar) readFrameSlotNode.executeRead(frame);
		Object value = valueNode.execute(frame);
		var.bind(value);
		return value;
	}

}
