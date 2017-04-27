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

	public static CopyVariableToFrameNode create(OzNode readNode, FrameSlot to) {
		return CopyVariableToFrameNodeGen.create(readNode, to, null);
	}

	public abstract Object executeWrite(VirtualFrame frame, MaterializedFrame capture);

	@Child OzNode readNode;
	protected final FrameSlot to;

	protected CopyVariableToFrameNode(OzNode readNode, FrameSlot to) {
		this.readNode = readNode;
		this.to = to;
	}

	@Specialization
	public Object write(VirtualFrame frame, MaterializedFrame capture,
			@Cached("create(to)") WriteFrameSlotNode writeNode) {
		writeNode.executeWrite(capture, readNode.execute(frame));
		return unit;
	}

}
