package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChildren({ @NodeChild("var"), @NodeChild("value") })
public abstract class BindVariableValueNode extends OzNode {

	@Child WriteFrameSlotNode writeFrameSlotNode;

	public BindVariableValueNode(WriteFrameSlotNode writeFrameSlotNode) {
		this.writeFrameSlotNode = writeFrameSlotNode;
	}

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNodeGen.create(value);
	}

	@Specialization
	public Object bind(VirtualFrame frame, OzVar var, Object value) {
		assert !(value instanceof OzVar);
		// Write to the OzVar in the store, in case there is another reference
		// to it
		var.bind(value);
		// Also write the value directly to the frame slot
		writeFrameSlotNode.write(frame, value);
		return var;
	}

}
