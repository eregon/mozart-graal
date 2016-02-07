package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.nodes.local.WriteFrameSlotNode;
import org.mozartoz.truffle.runtime.Nil;
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
		writeValues[0].executeWrite(frame, cons.getHead());
		writeValues[1].executeWrite(frame, cons.getTail());
		return true;
	}

	@Specialization
	boolean patternMatch(Nil nil) {
		return false;
	}

}
