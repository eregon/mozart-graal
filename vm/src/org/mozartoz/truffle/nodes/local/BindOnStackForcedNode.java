package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class BindOnStackForcedNode extends OzNode {

	final FrameSlot slot; // For the serializer
	@Child OzNode rightNode;

	@Child WriteNode writeSlot;
	@Child DerefIfBoundNode rightDerefNode = DerefIfBoundNode.create();

	public BindOnStackForcedNode(FrameSlot slot, OzNode rightNode) {
		this.slot = slot;
		this.rightNode = rightNode;
		this.writeSlot = WriteFrameSlotNodeGen.create(slot);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object right = rightDerefNode.executeDerefIfBound(rightNode.execute(frame));
		writeSlot.write(frame, right);
		return right;
	}

}
