package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class AndNode extends OzNode {

	@Children final OzNode[] conditions;

	public AndNode(OzNode[] conditions) {
		this.conditions = conditions;
	}

	@Override
	@ExplodeLoop
	public Object execute(VirtualFrame frame) {
		for (OzNode condition : conditions) {
			if (!(boolean) condition.execute(frame)) {
				return false;
			}
		}
		return true;
	}

}
