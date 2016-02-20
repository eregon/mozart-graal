package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.WriteNode;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.translator.FrameSlotAndDepth;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class TryNode extends OzNode {

	@Child OzNode body;
	@Child OzNode catchBody;
	@Child WriteNode writeExceptionVarNode;

	BranchProfile exceptionProfile = BranchProfile.create();

	public TryNode(FrameSlotAndDepth exceptionVarSlot, OzNode body, OzNode catchBody) {
		this.body = body;
		this.catchBody = catchBody;
		this.writeExceptionVarNode = exceptionVarSlot.createWriteNode();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		try {
			return body.execute(frame);
		} catch (OzException exception) {
			exceptionProfile.enter();
			writeExceptionVarNode.write(frame, exception);
			return catchBody.execute(frame);
		}
	}

}
