package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
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
public abstract class DFSUnifyNode extends OzNode {

	public static DFSUnifyNode create() {
		return DFSUnifyNodeGen.create(null, null);
	}

	// Should undo bindings if unification fails?
	// Actually, no, as stated in PVR's book

	@Child DerefIfBoundNode derefIfBoundNode = DerefIfBoundNodeGen.create();
	@Child DFSEqualNode equalNode;

	public abstract Object executeUnify(Object a, Object b);

	@TruffleBoundary
	protected Object unify(Object a, Object b) {
		return executeUnify(deref(a), deref(b));
	}

	@Specialization(guards = { "!a.isBound()", "!b.isBound()" })
	Object unifyUnboundUnbound(Variable a, Variable b) {
		a.link(b);
		return a;
	}

	@Specialization(guards = { "isBound(a)", "!isBound(b)" })
	Object unifyLeftBound(OzVar a, OzVar b) {
		Object value = a.getBoundValue(this);
		b.bind(value);
		return value;
	}

	@Specialization(guards = { "!isBound(a)", "isBound(b)" })
	Object unifyRightBound(OzVar a, OzVar b) {
		Object value = b.getBoundValue(this);
		a.bind(value);
		return value;
	}

	@Specialization(guards = { "!isVariable(a)", "!isBound(b)" })
	Object unify(Object a, OzVar b) {
		b.bind(a);
		return a;
	}

	@Specialization(guards = { "!isBound(a)", "!isVariable(b)" })
	Object unify(OzVar a, Object b) {
		a.bind(b);
		return b;
	}

	@Specialization
	Object unify(OzCons a, OzCons b) {
		unify(a.getHead(), b.getHead());
		unify(a.getTail(), b.getTail());
		return a;
	}

	@Specialization(guards = "a.getShape() == b.getShape()")
	Object unify(DynamicObject a, DynamicObject b) {
		for (Property property : a.getShape().getProperties()) {
			Object aValue = property.get(a, a.getShape());
			Object bValue = property.get(b, b.getShape());
			unify(aValue, bValue);
		}
		return a;
	}

	@Specialization(guards = { "!isVariable(a)", "!isVariable(b)",
			"!isCons(a) || !isCons(b)", // not to conflict with other specializations
			"!isRecord(a) || !isRecord(b)" })
	Object unifyValues(Object a, Object b) {
		if (!equal(a, b)) {
			CompilerDirectives.transferToInterpreter();
			failUnification(a, b);
		}
		return a;
	}

	protected Object deref(Object value) {
		return derefIfBoundNode.executeDerefIfBound(value);
	}

	private boolean equal(Object a, Object b) {
		if (equalNode == null) {
			CompilerDirectives.transferToInterpreter();
			equalNode = insert(DFSEqualNode.create());
		}
		return equalNode.executeEqual(a, b);
	}

	public void failUnification(Object a, Object b) {
		throw new OzException(this, "Failed unification: " + a + " != " + b);
	}

}
