package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.utilities.ConditionProfile;

public class IfNode extends OzNode {

	@Child OzNode condition;
	@Child OzNode thenExpr;
	@Child OzNode elseExpr;

	private final ConditionProfile profile = ConditionProfile.createCountingProfile();

	public IfNode(OzNode condition, OzNode thenExpr, OzNode elseExpr) {
		this.condition = condition;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		if (profile.profile((boolean) condition.execute(frame))) {
			return thenExpr.execute(frame);
		} else {
			return elseExpr.execute(frame);
		}
	}

}
