package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild("capture")
public abstract class CopyVariableToFrameNode extends OzNode {

	public static CopyVariableToFrameNode create(OzNode readNode, FrameSlot slot) {
		return CopyVariableToFrameNodeGen.create(readNode, slot, null);
	}

	public abstract Object executeWrite(VirtualFrame frame, MaterializedFrame capture);

	@Child OzNode readNode;

	public final FrameSlot slot;

	protected CopyVariableToFrameNode(OzNode readNode, FrameSlot slot) {
		this.readNode = readNode;
		this.slot = slot;
	}

	public OzNode getReadNode() {
		return readNode;
	}

	@Specialization
	public Object write(VirtualFrame frame, MaterializedFrame capture,
			@Cached("create(slot)") WriteFrameSlotNode writeNode) {
		writeNode.executeWrite(capture, readNode.execute(frame));
		return unit;
	}

}
