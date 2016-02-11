package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.nodes.local.WriteFrameSlotNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild("value")
public abstract class PatternMatchConsNode extends OzNode {

	@Children final WriteFrameSlotNode[] writeValues;

	public PatternMatchConsNode(WriteFrameSlotNode[] writeValues) {
		this.writeValues = writeValues;
		assert writeValues.length == 2;
	}

	@Specialization
	boolean patternMatch(VirtualFrame frame, OzCons cons) {
		if (writeValues[0] != null) {
			writeValues[0].write(frame, cons.getHead());
		}
		if (writeValues[1] != null) {
			writeValues[1].write(frame, cons.getTail());
		}
		return true;
	}

	@Specialization(guards = "isNil(nil)")
	boolean patternMatch(String nil) {
		return false;
	}

}
