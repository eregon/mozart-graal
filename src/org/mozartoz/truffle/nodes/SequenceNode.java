package org.mozartoz.truffle.nodes;

import java.util.Arrays;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class SequenceNode extends OzNode {

	@Children final OzNode[] statements;

	public static OzNode sequence(OzNode... nodes) {
		return new SequenceNode(nodes);
	}

	public static OzNode sequence(OzNode[] nodes, OzNode last) {
		if (nodes.length == 0) {
			return last;
		}
		OzNode[] all = Arrays.copyOf(nodes, nodes.length + 1);
		all[all.length - 1] = last;
		return new SequenceNode(all);
	}

	private SequenceNode(OzNode... statements) {
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
