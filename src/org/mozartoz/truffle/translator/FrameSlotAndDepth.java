package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.local.ReadOzVarNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.local.WriteLocalVariableNode;

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
		if (depth == 0) {
			return new WriteLocalVariableNode(slot, value);
		} else {
			throw new RuntimeException("" + depth);
		}
	}

	public OzNode createGetOzVarNode() {
		if (depth == 0) {
			return new ReadOzVarNode(slot);
		} else {
			throw new RuntimeException("" + depth);
		}
	}

	public WriteFrameSlotNode createSetOzVarNode() {
		if (depth == 0) {
			return WriteFrameSlotNodeGen.create(slot);
		} else {
			throw new RuntimeException("" + depth);
		}
	}

}
