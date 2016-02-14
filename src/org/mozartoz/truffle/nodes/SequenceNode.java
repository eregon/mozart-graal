package org.mozartoz.truffle.nodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class SequenceNode extends OzNode {

	@Children final OzNode[] statements;

	public static OzNode sequence(OzNode... nodes) {
		return singleOrSeq(nodes);
	}

	public static OzNode sequence(OzNode[] nodes, OzNode last) {
		if (nodes.length == 0) {
			return last;
		}
		OzNode[] all = Arrays.copyOf(nodes, nodes.length + 1);
		all[all.length - 1] = last;
		return singleOrSeq(all);
	}

	private static OzNode singleOrSeq(OzNode[] nodes) {
		OzNode[] flat = flatten(nodes);
		return flat.length == 1 ? flat[0] : new SequenceNode(flat);
	}

	private static OzNode[] flatten(OzNode[] nodes) {
		Deque<OzNode> stack = new ArrayDeque<>(nodes.length);
		for (OzNode node : nodes) {
			stack.addFirst(node);
		}
		List<OzNode> flat = new ArrayList<OzNode>(nodes.length);

		while (!stack.isEmpty()) {
			OzNode node = stack.removeLast();
			if (node instanceof SequenceNode) {
				OzNode[] subs = ((SequenceNode) node).statements;
				for (int i = subs.length - 1; i >= 0; i--) {
					stack.addLast(subs[i]);
				}
			} else {
				flat.add(node);
			}
		}
		return flat.toArray(new OzNode[flat.size()]);
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
