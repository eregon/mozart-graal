package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class InitializeVarNode extends OzNode {

	final FrameSlot slot;

	public InitializeVarNode(FrameSlot slot) {
		this.slot = slot;
	}

	public FrameSlot getSlot() {
		return slot;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		frame.setObject(slot, new OzVar());
		return unit;
	}

	@Override
	public String toString() {
		return super.toString() + " " + slot.getIdentifier();
	}

}
