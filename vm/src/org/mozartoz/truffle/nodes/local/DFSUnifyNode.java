package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.profiles.BranchProfile;
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

@NodeChildren({ @NodeChild("left"), @NodeChild("right"), @NodeChild("acc") })
public abstract class DFSUnifyNode extends OzNode {

	public static DFSUnifyNode create() {
		return DFSUnifyNodeGen.create(null, null, null);
	}

	// Should undo bindings if unification fails?
	// Actually, no, as stated in PVR's book

	@Child DerefIfBoundNode derefIfBoundNode = DerefIfBoundNodeGen.create();
	@Child DFSEqualNode equalNode;

	public abstract Object executeUnify(Object a, Object b, Object state);

	/**
	 * Put before recursive unification, to create state if it does not exist
	 */
	protected Object needState(Object state) {
		if (state == null) {
			return initState();
		}
		return state;
	}

	protected Object initState() {
		return null;
	}

	@TruffleBoundary
	protected Object unify(Object a, Object b, Object state) {
		return executeUnify(deref(a), deref(b), state);
	}

	@Specialization(guards = { "!a.isBound()", "!b.isBound()" })
	Object unifyUnboundUnbound(Variable a, Variable b, Object state) {
		a.link(b);
		return a;
	}

	@Specialization(guards = { "isBound(a)", "!isBound(b)" })
	Object unifyLeftBound(OzVar a, OzVar b, Object state) {
		Object value = a.getBoundValue(this);
		b.bind(value);
		return value;
	}

	@Specialization(guards = { "!isBound(a)", "isBound(b)" })
	Object unifyRightBound(OzVar a, OzVar b, Object state) {
		Object value = b.getBoundValue(this);
		a.bind(value);
		return value;
	}

	@Specialization(guards = { "!isVariable(a)", "!isBound(b)" })
	Object unify(Object a, OzVar b, Object state) {
		b.bind(a);
		return a;
	}

	@Specialization(guards = { "!isBound(a)", "!isVariable(b)" })
	Object unify(OzVar a, Object b, Object state) {
		a.bind(b);
		return b;
	}

	@Specialization
	Object unify(OzCons a, OzCons b, Object state) {
		Object newState = needState(state);
		unify(a.getHead(), b.getHead(), newState);
		unify(a.getTail(), b.getTail(), newState);
		return a;
	}

	@TruffleBoundary
	@Specialization(guards = "a.getShape() == b.getShape()")
	Object unify(DynamicObject a, DynamicObject b, Object state) {
		Object newState = needState(state);
		for (Property property : a.getShape().getProperties()) {
			Object aValue = property.get(a, a.getShape());
			Object bValue = property.get(b, b.getShape());
			unify(aValue, bValue, newState);
		}
		return a;
	}

	@Specialization(guards = { "!isVariable(a)", "!isVariable(b)",
			"!isCons(a) || !isCons(b)", // not to conflict with other specializations
			"!isRecord(a) || !isRecord(b)" })
	Object unifyValues(Object a, Object b, Object state,
			@Cached BranchProfile failureProfile) {
		if (!equal(a, b)) {
			failureProfile.enter();
			failUnification(a, b);
		}
		return a;
	}

	protected Object deref(Object value) {
		return derefIfBoundNode.executeDerefIfBound(value);
	}

	private boolean equal(Object a, Object b) {
		if (equalNode == null) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			equalNode = insert(DFSEqualNode.create());
		}
		return equalNode.executeEqual(a, b, null);
	}

	public void failUnification(Object a, Object b) {
		throw new OzException(this, OzException.newFailure());
	}

}
