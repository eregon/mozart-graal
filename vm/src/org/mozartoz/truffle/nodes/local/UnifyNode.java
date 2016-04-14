package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltins.EqualNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class UnifyNode extends OzNode {

	public static UnifyNode create() {
		return UnifyNodeGen.create(null, null);
	}

	// TODO: should undo bindings if unification fails.
	// Maybe by catching the failed unification exception?

	@Child DerefIfBoundNode derefIfBoundNode = DerefIfBoundNodeGen.create();
	@Child EqualNode equalNode;

	public abstract Object executeUnify(Object a, Object b);

	@TruffleBoundary
	private Object unify(Object a, Object b) {
		return executeUnify(deref(a), deref(b));
	}

	@Specialization(guards = { "!a.isBound()", "!b.isBound()" })
	Object unifyUnboundUnbound(Variable a, Variable b) {
		a.link(b);
		return unit;
	}

	@Specialization(guards = { "isBound(a)", "!isBound(b)" })
	Object unifyLeftBound(OzVar a, OzVar b) {
		b.bind(a.getBoundValue(this));
		return unit;
	}

	@Specialization(guards = { "!isBound(a)", "isBound(b)" })
	Object unifyRightBound(OzVar a, OzVar b) {
		a.bind(b.getBoundValue(this));
		return unit;
	}

	@Specialization(guards = { "!isVariable(a)", "!isBound(b)" })
	Object unify(Object a, OzVar b) {
		b.bind(a);
		return unit;
	}

	@Specialization(guards = { "!isBound(a)", "!isVariable(b)" })
	Object unify(OzVar a, Object b) {
		a.bind(b);
		return unit;
	}

	@Specialization
	Object unify(OzCons a, OzCons b) {
		unify(a.getHead(), b.getHead());
		unify(a.getTail(), b.getTail());
		return unit;
	}

	@Specialization(guards = "a.getShape() == b.getShape()")
	Object unify(DynamicObject a, DynamicObject b) {
		for (Property property : a.getShape().getProperties()) {
			Object aValue = property.get(a, a.getShape());
			Object bValue = property.get(b, b.getShape());
			unify(aValue, bValue);
		}
		return unit;
	}

	@Specialization(guards = { "!isVariable(a)", "!isVariable(b)", "!isCons(a)", "!isCons(b)" })
	Object unifyValues(Object a, Object b) {
		if (!equal(a, b)) {
			CompilerDirectives.transferToInterpreter();
			throw new OzException(this, "Failed unification: " + a + " != " + b);
		}
		return unit;
	}

	private Object deref(Object value) {
		return derefIfBoundNode.executeDerefIfBound(value);
	}

	private boolean equal(Object a, Object b) {
		if (equalNode == null) {
			CompilerDirectives.transferToInterpreter();
			equalNode = insert(EqualNode.create());
		}
		return equalNode.executeEqual(a, b);
	}

}
