package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class InitializeTmpNode extends OzNode {

	final FrameSlot slot;
	@Child OzNode value;

	public InitializeTmpNode(FrameSlot slot, OzNode value) {
		this.slot = slot;
		this.value = value;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		frame.setObject(slot, value.execute(frame));
		return unit;
	}

}
