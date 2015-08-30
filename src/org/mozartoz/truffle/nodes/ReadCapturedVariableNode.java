package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadCapturedVariableNode extends OzNode {

	private final FrameSlot slot;

	public ReadCapturedVariableNode(FrameSlot slot) {
		this.slot = slot;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		MaterializedFrame parentFrame = OzArguments.getParentFrame(frame);
		return parentFrame.getValue(slot);
	}

}
