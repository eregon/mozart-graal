package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class IfNode extends OzNode {

	@Child OzNode condition;
	@Child OzNode thenExpr;
	@Child OzNode elseExpr;

	@Child DerefNode derefNode;

	private final ConditionProfile profile = ConditionProfile.createCountingProfile();

	public IfNode(OzNode condition, OzNode thenExpr, OzNode elseExpr) {
		this.condition = condition;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;
		this.derefNode = DerefNode.create();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		if (profile.profile((boolean) derefNode.executeDeref(condition.execute(frame)))) {
			return thenExpr.execute(frame);
		} else {
			return elseExpr.execute(frame);
		}
	}

}
