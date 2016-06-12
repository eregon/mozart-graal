package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.WriteNode;
import org.mozartoz.truffle.runtime.OzException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class TryNode extends OzNode {

	@Child OzNode body;
	@Child OzNode catchBody;
	@Child WriteNode writeExceptionVarNode;

	BranchProfile exceptionProfile = BranchProfile.create();

	public TryNode(WriteNode writeExceptionVarNode, OzNode body, OzNode catchBody) {
		this.body = body;
		this.catchBody = catchBody;
		this.writeExceptionVarNode = writeExceptionVarNode;
	}

	public OzNode getBody() {
		return body;
	}

	public OzNode getCatchBody() {
		return catchBody;
	}

	public WriteNode getWriteExceptionVarNode() {
		return writeExceptionVarNode;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		try {
			return body.execute(frame);
		} catch (OzException exception) {
			exceptionProfile.enter();
			writeExceptionVarNode.write(frame, exception.getData());
			return catchBody.execute(frame);
		}
	}

}
