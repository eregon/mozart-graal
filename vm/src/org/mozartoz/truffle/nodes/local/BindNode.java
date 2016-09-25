package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class BindNode extends OzNode {

	@Child UnifyNode unifyNode;

	@CreateCast("left")
	protected OzNode derefLeft(OzNode var) {
		return DerefIfBoundNode.create(var);
	}

	@CreateCast("right")
	protected OzNode derefRight(OzNode var) {
		return DerefIfBoundNode.create(var);
	}

	public abstract Object executeBind(Object a, Object b);

	@Specialization(guards = { "!left.isBound()", "!right.isBound()" })
	Object unboundUnbound(Variable left, Variable right) {
		left.link(right);
		return left;
	}

	@Specialization(guards = { "!left.isBound()", "right.isBound()" })
	Object bindLeft(OzVar left, OzVar right) {
		Object value = right.getBoundValue(this);
		left.bind(value);
		return value;
	}

	@Specialization(guards = { "!left.isBound()", "!isVariable(right)" })
	Object bindLeftValue(OzVar left, Object right) {
		left.bind(right);
		// TODO: Also write the value directly to the frame slot if writeLeft not null?
		return right;
	}

	@Specialization(guards = { "left.isBound()", "!right.isBound()" })
	Object bindRight(OzVar left, OzVar right) {
		Object value = left.getBoundValue(this);
		right.bind(value);
		return value;
	}

	@Specialization(guards = { "!isVariable(left)", "!right.isBound()" })
	Object bindRightValue(Object left, OzVar right) {
		right.bind(left);
		return left;
	}

	@Specialization(guards = { "!isVariable(left)", "!isVariable(right)" })
	Object bindValues(Object left, Object right) {
		return unifyValues(left, right);
	}

	@Specialization(guards = { "isBound(left)", "!isVariable(right)" })
	Object bindVarValue(OzVar left, Object right) {
		return unifyValues(left.getBoundValue(this), right);
	}

	@Specialization(guards = { "!isVariable(left)", "isBound(right)" })
	Object bindValueVar(Object left, OzVar right) {
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
