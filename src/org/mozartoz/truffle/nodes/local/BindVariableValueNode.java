package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.FrameSlotAndDepth;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

@NodeChild("value")
public abstract class BindVariableValueNode extends OzNode {

	final int depth;

	@Child ReadFrameSlotNode readFrameSlotNode;
	@Child WriteFrameSlotNode writeFrameSlotNode;

	public BindVariableValueNode(FrameSlotAndDepth slot) {
		this.depth = slot.getDepth();
		this.readFrameSlotNode = slot.createReadSlotNode();
		this.writeFrameSlotNode = slot.createWriteSlotNode();
	}

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNodeGen.create(value);
	}

	@Specialization
	public Object bind(VirtualFrame topFrame, Object value) {
		assert !(value instanceof OzVar);

		Frame frame = OzArguments.getParentFrame(topFrame, depth);
		OzVar var = (OzVar) readFrameSlotNode.executeRead(frame);
		// Write to the OzVar in the store, in case there is another reference
		// to it
		var.bind(value);
		// Also write the value directly to the frame slot
		writeFrameSlotNode.write(frame, value);
		return var;
	}

	@ExplodeLoop
	private Frame getFrame(Frame frame, int depth) {
		for (int i = 0; i < depth; i++) {
			frame = OzArguments.getParentFrame(frame);
		}
		return frame;
	}

}
