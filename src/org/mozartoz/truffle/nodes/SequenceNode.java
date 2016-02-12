package org.mozartoz.truffle.nodes;

import java.util.List;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class SequenceNode extends OzNode {

	@Children final OzNode[] statements;

	public static OzNode sequence(List<OzNode> nodes, OzNode last) {
		if (nodes.size() == 0) {
			return last;
		}
		OzNode[] all = new OzNode[nodes.size() + 1];
		nodes.toArray(all);
		all[all.length - 1] = last;
		return new SequenceNode(all);
	}

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
