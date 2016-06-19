package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class AndNode extends OzNode {

	@Children final OzNode[] conditions;

	public AndNode(OzNode[] conditions) {
		this.conditions = deref(conditions);
	}

	static OzNode[] deref(OzNode[] values) {
		OzNode[] deref = new OzNode[values.length];
		for (int i = 0; i < values.length; i++) {
			deref[i] = DerefNodeGen.create(values[i]);
		}
		return deref;
	}

	public OzNode[] getConditions() {
		return conditions;
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
