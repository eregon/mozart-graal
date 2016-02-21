package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.WriteNode;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.FrameSlotAndDepth;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChildren({ @NodeChild("var"), @NodeChild("value") })
public abstract class PatternMatchCaptureNode extends OzNode {

	@Child WriteNode writeVarNode;

	public PatternMatchCaptureNode(FrameSlotAndDepth varSlot) {
		writeVarNode = varSlot.createWriteNode();
	}

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefIfBoundNodeGen.create(value);
	}

	@Specialization(guards = "!isVar(value)")
	Object capture(OzVar var, Object value) {
		var.bind(value);
		return unit;
	}

	@Specialization(guards = "!isBound(value)")
	Object captureUnbound(VirtualFrame frame, OzVar var, OzVar value) {
		writeVarNode.write(frame, value);
		var.setDead();
		return unit;
	}

}
