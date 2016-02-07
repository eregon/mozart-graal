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
		readLeft = leftSlot.createReadNode();
		readRight = rightSlot.createReadNode();
		writeLeft = leftSlot.createSetNode();
		writeRight = rightSlot.createSetNode();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object left = readLeft.execute(frame);
		Object right = readRight.execute(frame);

		if (!(left instanceof OzVar)) {
			((OzVar) right).bind(left);
			return left;
		} else if (!(right instanceof OzVar)) {
			((OzVar) left).bind(right);
			return right;
		}

		OzVar leftVar = (OzVar) left;
		OzVar rightVar = (OzVar) right;

		if (!leftVar.isBound()) {
			writeLeft.executeWrite(frame, rightVar);
			return rightVar;
		} else if (!rightVar.isBound()) {
			writeRight.executeWrite(frame, leftVar);
			return leftVar;
		} else {
			throw new RuntimeException("bind bound-bound");
		}
	}

}
