package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltins.EqualNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.CompilerDirectives;
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

	@Child EqualNode equalNode;

	public abstract Object executeUnify(Object a, Object b);

	@Specialization(guards = { "!isVar(a)", "!isBound(b)" })
	Object unify(Object a, OzVar b) {
		b.bind(a);
		return unit;
	}

	@Specialization(guards = { "!isBound(a)", "!isVar(b)" })
	Object unify(OzVar a, Object b) {
		a.bind(b);
		return unit;
	}

	@Specialization
	Object unify(OzCons a, OzCons b) {
		executeUnify(a.getHead(), b.getHead());
		executeUnify(a.getTail(), b.getTail());
		return unit;
	}

	@Specialization(guards = "a.getShape() == b.getShape()")
	Object unify(DynamicObject a, DynamicObject b) {
		for (Property property : a.getShape().getProperties()) {
			Object aValue = property.get(a, a.getShape());
			Object bValue = property.get(b, b.getShape());
			executeUnify(aValue, bValue);
		}
		return unit;
	}

	@Specialization(guards = { "!isVar(a)", "!isVar(b)", "!isCons(a)", "!isCons(b)" })
	Object unify(Object a, Object b) {
		if (!equal(a, b)) {
			CompilerDirectives.transferToInterpreter();
			throw new OzException(this, "Failed unification: " + a + " != " + b);
		}
		return unit;
	}

	private boolean equal(Object a, Object b) {
		if (equalNode == null) {
			CompilerDirectives.transferToInterpreter();
			equalNode = insert(EqualNode.create());
		}
		return equalNode.executeEqual(a, b);
	}

}
