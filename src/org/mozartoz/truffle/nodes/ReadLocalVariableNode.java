package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadLocalVariableNode extends OzNode {

	private final FrameSlot slot;

	public ReadLocalVariableNode(FrameSlot slot) {
		this.slot = slot;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return frame.getValue(slot);
	}

}
