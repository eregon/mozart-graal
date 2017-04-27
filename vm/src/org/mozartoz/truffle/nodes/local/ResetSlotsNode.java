package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class ResetSlotsNode extends OzNode {

	@CompilationFinal(dimensions = 1) FrameSlot[] before;
	@Child OzNode node;
	@CompilationFinal(dimensions = 1) FrameSlot[] after;

	public ResetSlotsNode(FrameSlot[] before, OzNode node, FrameSlot[] after) {
		this.before = before;
		this.node = node;
		this.after = after;
	}

	@ExplodeLoop
	private void resetSlots(VirtualFrame frame, FrameSlot[] slots) {
		for (int i = 0; i < slots.length; i++) {
			frame.setObject(slots[i], null);
		}
	}

	@Override
	public Object execute(VirtualFrame frame) {
		resetSlots(frame, before);
		Object result = node.execute(frame);
		resetSlots(frame, after);
		return result;
	}

}
