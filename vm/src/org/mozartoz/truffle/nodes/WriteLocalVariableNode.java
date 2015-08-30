package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild("value")
public abstract class WriteLocalVariableNode extends OzNode {

	private final FrameSlot slot;

	public WriteLocalVariableNode(FrameSlot slot) {
		this.slot = slot;
	}

	@Specialization
	public long writeLong(VirtualFrame frame, long value) {
		frame.setLong(slot, value);
		return value;
	}

	@Specialization
	public Object writeObject(VirtualFrame frame, Object value) {
		frame.setObject(slot, value);
		return value;
	}

}
