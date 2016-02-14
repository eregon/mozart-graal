package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.EqualNode;
import org.mozartoz.truffle.nodes.builtins.EqualNodeGen;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.FrameSlotAndDepth;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class BindNode extends OzNode {

	@Child WriteNode writeLeft;
	@Child WriteNode writeRight;

	@Child EqualNode equalNode;
	@Child UnifyNode unifyNode;

	public BindNode(FrameSlotAndDepth leftSlot, FrameSlotAndDepth rightSlot) {
		if (leftSlot != null) {
			writeLeft = leftSlot.createWriteNode();
		}
		if (rightSlot != null) {
			writeRight = rightSlot.createWriteNode();
		}
	}

	@Specialization(guards = { "!left.isBound()", "!right.isBound()" })
	Object assignLeft(VirtualFrame frame, OzVar left, OzVar right) {
		writeLeft.write(frame, right);
		left.setDead();
		// TODO: should chain left to right in case it was captured earlier
		return unit;
	}

	@Specialization(guards = { "!left.isBound()", "right.isBound()" })
	Object bindLeft(VirtualFrame frame, OzVar left, OzVar right) {
		left.bind(right.getBoundValue());
		return unit;
	}

	@Specialization(guards = { "!left.isBound()", "!isVar(right)" })
	Object bindLeftValue(VirtualFrame frame, OzVar left, Object right) {
		left.bind(right);
		// TODO: Also write the value directly to the frame slot if writeLeft not null?
		return unit;
	}

	@Specialization(guards = { "left.isBound()", "!right.isBound()" })
	Object bindRight(VirtualFrame frame, OzVar left, OzVar right) {
		right.bind(left.getBoundValue());
		return unit;
	}

	@Specialization(guards = { "!isVar(left)", "!right.isBound()" })
	Object bindRightValue(VirtualFrame frame, Object left, OzVar right) {
		right.bind(left);
		return unit;
	}

	@Specialization(guards = { "!isVar(left)", "!isVar(right)" })
	Object unifyValues(VirtualFrame frame, Object left, Object right) {
		return unifyValues(left, right);
	}

	@Specialization(guards = { "isBound(left)", "!isVar(right)" })
	Object unifyVarValue(VirtualFrame frame, OzVar left, Object right) {
		return unifyValues(left.getBoundValue(), right);
	}

	@Specialization(guards = { "!isVar(left)", "isBound(right)" })
	Object unifyValueVar(VirtualFrame frame, Object left, OzVar right) {
		return unifyValues(left, right.getBoundValue());
	}

	private Object unifyValues(Object left, Object right) {
		if (equal(left, right)) {
			return unit;
		} else {
			return unify(left, right);
		}
	}

	private boolean equal(Object a, Object b) {
		if (equalNode == null) {
			CompilerDirectives.transferToInterpreter();
			equalNode = insert(EqualNodeGen.create(null, null));
		}
		return equalNode.executeEqual(a, b);
	}

	private Object unify(Object a, Object b) {
		if (unifyNode == null) {
			CompilerDirectives.transferToInterpreter();
			unifyNode = insert(UnifyNodeGen.create(null, null));
		}
		return unifyNode.executeUnify(a, b);
	}

}
