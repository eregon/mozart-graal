package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Variable;
import org.mozartoz.truffle.translator.FrameSlotAndDepth;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class BindNode extends OzNode {

	@Child WriteNode writeLeft;

	@Child UnifyNode unifyNode;

	public BindNode(FrameSlotAndDepth leftSlot) {
		if (leftSlot != null) {
			writeLeft = leftSlot.createWriteNode();
		}
	}

	@CreateCast("left")
	protected OzNode derefLeft(OzNode var) {
		return DerefIfBoundNodeGen.create(var);
	}

	@CreateCast("right")
	protected OzNode derefRight(OzNode var) {
		return DerefIfBoundNodeGen.create(var);
	}

	@Specialization(guards = { "!left.isBound()", "!right.isBound()" })
	Object unboundUnbound(VirtualFrame frame, Variable left, Variable right) {
		left.link(right);
		return unit;
	}

	@Specialization(guards = { "!left.isBound()", "right.isBound()" })
	Object bindLeft(VirtualFrame frame, OzVar left, OzVar right) {
		left.bind(right.getBoundValue(this));
		return unit;
	}

	@Specialization(guards = { "!left.isBound()", "!isVariable(right)" })
	Object bindLeftValue(VirtualFrame frame, OzVar left, Object right) {
		left.bind(right);
		// TODO: Also write the value directly to the frame slot if writeLeft not null?
		return unit;
	}

	@Specialization(guards = { "left.isBound()", "!right.isBound()" })
	Object bindRight(VirtualFrame frame, OzVar left, OzVar right) {
		right.bind(left.getBoundValue(this));
		return unit;
	}

	@Specialization(guards = { "!isVariable(left)", "!right.isBound()" })
	Object bindRightValue(VirtualFrame frame, Object left, OzVar right) {
		right.bind(left);
		return unit;
	}

	@Specialization(guards = { "!isVariable(left)", "!isVariable(right)" })
	Object unifyValues(VirtualFrame frame, Object left, Object right) {
		return unifyValues(left, right);
	}

	@Specialization(guards = { "isBound(left)", "!isVariable(right)" })
	Object unifyVarValue(VirtualFrame frame, OzVar left, Object right) {
		return unifyValues(left.getBoundValue(this), right);
	}

	@Specialization(guards = { "!isVariable(left)", "isBound(right)" })
	Object unifyValueVar(VirtualFrame frame, Object left, OzVar right) {
		return unifyValues(left, right.getBoundValue(this));
	}

	private Object unifyValues(Object left, Object right) {
		return unify(left, right);
	}

	private Object unify(Object a, Object b) {
		if (unifyNode == null) {
			CompilerDirectives.transferToInterpreter();
			unifyNode = insert(UnifyNode.create());
		}
		return unifyNode.executeUnify(a, b);
	}

}
