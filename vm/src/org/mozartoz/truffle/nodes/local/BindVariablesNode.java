package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.FrameSlotAndDepth;

import com.oracle.truffle.api.frame.VirtualFrame;

public class BindVariablesNode extends OzNode {

	@Child OzNode readLeft;
	@Child OzNode readRight;

	@Child WriteFrameSlotNode writeLeft;
	@Child WriteFrameSlotNode writeRight;

	public BindVariablesNode(FrameSlotAndDepth leftSlot, FrameSlotAndDepth rightSlot) {
		readLeft = leftSlot.createGetOzVarNode();
		readRight = rightSlot.createGetOzVarNode();
		writeLeft = leftSlot.createSetOzVarNode();
		writeRight = rightSlot.createSetOzVarNode();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		OzVar leftVar = (OzVar) readLeft.execute(frame);
		OzVar rightVar = (OzVar) readRight.execute(frame);

		if (!leftVar.isBound()) {
			writeLeft.executeWrite(frame, rightVar);
			return rightVar.getValue();
		} else if (!rightVar.isBound()) {
			writeRight.executeWrite(frame, leftVar);
			return leftVar.getValue();
		} else {
			throw new RuntimeException("bind bound-bound");
		}
	}

}
