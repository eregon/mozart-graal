package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.ReadFrameSlotNode;
import org.mozartoz.truffle.nodes.local.ReadFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.local.WriteCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.local.WriteLocalVariableNode;
import org.mozartoz.truffle.nodes.local.WriteNode;

import com.oracle.truffle.api.frame.FrameSlot;

public class FrameSlotAndDepth {
	final FrameSlot slot;
	final int depth;

	public FrameSlotAndDepth(FrameSlot frameSlot, int depth) {
		this.slot = frameSlot;
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public ReadFrameSlotNode createReadSlotNode() {
		return ReadFrameSlotNodeGen.create(slot);
	}

	public WriteFrameSlotNode createWriteSlotNode() {
		return WriteFrameSlotNodeGen.create(slot);
	}

	public OzNode createReadNode() {
		if (depth == 0) {
			return new ReadLocalVariableNode(slot);
		} else {
			return new ReadCapturedVariableNode(slot, depth);
		}
	}

	public WriteNode createWriteNode() {
		if (depth == 0) {
			return new WriteLocalVariableNode(slot);
		} else {
			return new WriteCapturedVariableNode(slot, depth);
		}
	}

}
