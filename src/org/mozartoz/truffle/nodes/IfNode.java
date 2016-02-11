package org.mozartoz.truffle.nodes;

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
		this.derefNode = DerefNodeGen.create(null);
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
