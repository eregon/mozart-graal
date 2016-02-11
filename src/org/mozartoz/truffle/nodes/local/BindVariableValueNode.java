package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.EqualNode;
import org.mozartoz.truffle.nodes.builtins.EqualNodeGen;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.FrameSlotAndDepth;

import com.oracle.truffle.api.CompilerDirectives;
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

	@Child EqualNode equalNode;

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
		Object left = readFrameSlotNode.executeRead(frame);
		Object leftValue = null;
		if (left instanceof OzVar) {
			OzVar var = (OzVar) left;
			if (var.isBound()) {
				leftValue = var.getValue();
			}
		} else {
			leftValue = left;
		}

		if (leftValue != null) {
			if (equal(leftValue, value)) {
				return value;
			} else {
				throw new RuntimeException("Failed unification: " + leftValue + " != " + value);
			}
		} else {
			OzVar var = (OzVar) left;
			// Write to the OzVar in the store, in case there is another
			// reference to it
			var.bind(value);
			// Also write the value directly to the frame slot
			writeFrameSlotNode.write(frame, value);
			return var;
		}
	}

	private boolean equal(Object a, Object b) {
		if (equalNode == null) {
			CompilerDirectives.transferToInterpreter();
			equalNode = insert(EqualNodeGen.create(null, null));
		}
		return equalNode.executeEqual(a, b);
	}

	@ExplodeLoop
	private Frame getFrame(Frame frame, int depth) {
		for (int i = 0; i < depth; i++) {
			frame = OzArguments.getParentFrame(frame);
		}
		return frame;
	}

}
