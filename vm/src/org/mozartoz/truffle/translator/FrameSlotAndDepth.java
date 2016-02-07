package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.BindVariableValueNodeGen;
import org.mozartoz.truffle.nodes.local.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNodeGen;

import com.oracle.truffle.api.frame.FrameSlot;

public class FrameSlotAndDepth {
	final FrameSlot slot;
	final int depth;

	public FrameSlotAndDepth(FrameSlot frameSlot, int depth) {
		this.slot = frameSlot;
		this.depth = depth;
	}

	public OzNode createReadNode() {
		if (depth == 0) {
			return new ReadLocalVariableNode(slot);
		} else if (depth == 1) {
			return new ReadCapturedVariableNode(slot);
		} else {
			throw new RuntimeException("" + depth);
		}
	}

	public OzNode createWriteNode(OzNode value) {
		if (depth != 0) {
			throw new RuntimeException("" + depth);
		}
		WriteFrameSlotNode writeFrameSlotNode = WriteFrameSlotNodeGen.create(slot);
		return BindVariableValueNodeGen.create(writeFrameSlotNode, createReadNode(), value);
	}

	public WriteFrameSlotNode createSetNode() {
		if (depth == 0) {
			return WriteFrameSlotNodeGen.create(slot);
		} else {
			throw new RuntimeException("" + depth);
		}
	}

}
