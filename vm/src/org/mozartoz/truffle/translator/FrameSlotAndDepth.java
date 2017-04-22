package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.ReadCapturedVariableNodeGen;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.local.WriteCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.local.WriteNode;

import com.oracle.truffle.api.frame.FrameSlot;

public class FrameSlotAndDepth {
	final FrameSlot slot;
	final int depth;

	public FrameSlotAndDepth(FrameSlot frameSlot) {
		this(frameSlot, 0);
	}

	public FrameSlotAndDepth(FrameSlot frameSlot, int depth) {
		this.slot = frameSlot;
		this.depth = depth;
	}

	public FrameSlot getSlot() {
		return slot;
	}

	public int getDepth() {
		return depth;
	}

	public OzNode createReadNode() {
		if (depth == 0) {
			return new ReadLocalVariableNode(slot);
		} else {
			return ReadCapturedVariableNodeGen.create(slot, depth);
		}
	}

	public WriteNode createWriteNode() {
		if (depth == 0) {
			return WriteFrameSlotNodeGen.create(slot);
		} else {
			return new WriteCapturedVariableNode(slot, depth);
		}
	}

}
