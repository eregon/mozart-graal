package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class SequenceNode extends OzNode {

	@Children final OzNode[] statements;

	public SequenceNode(OzNode... statements) {
		this.statements = statements;
	}

	@Override
	@ExplodeLoop
	public Object execute(VirtualFrame frame) {
		for (int i = 0; i < statements.length - 1; i++) {
			statements[i].execute(frame);
		}
		return statements[statements.length - 1].execute(frame);
	}

}
