package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class InitializeArgNode extends OzNode implements FrameSlotNode {

	@Child WriteFrameSlotNode writeFrameSlotNode;

	private int index;

	public InitializeArgNode(FrameSlot slot, int index) {
		this.writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
		this.index = index;
	}

	public FrameSlot getSlot() {
		return writeFrameSlotNode.getSlot();
	}

	public Object execute(VirtualFrame frame) {
		Object value = OzArguments.getArgument(frame, index);
		writeFrameSlotNode.write(frame, value);
		return unit;
	}

}
