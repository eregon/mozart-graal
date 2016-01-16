package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild("var")
public abstract class InitializeArgNode extends OzNode {

	@Child WriteFrameSlotNode writeFrameSlotNode;

	public InitializeArgNode(FrameSlot slot) {
		this.writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
	}

	@Specialization
	public Object initializeArg(VirtualFrame frame, Object value) {
		writeFrameSlotNode.executeWrite(frame, value);
		return unit;
	}

}
