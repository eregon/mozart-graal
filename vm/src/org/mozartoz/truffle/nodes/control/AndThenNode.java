package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class AndThenNode extends OzNode {

	@Child OzNode left;
	@Child OzNode right;

	private final ConditionProfile conditionProfile = ConditionProfile.createCountingProfile();

	public AndThenNode(OzNode left, OzNode right) {
		this.left = DerefNode.create(left);
		this.right = DerefNode.create(right);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return conditionProfile.profile((boolean) left.execute(frame)) && (boolean) right.execute(frame);
	}

}
