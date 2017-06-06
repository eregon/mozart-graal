package org.mozartoz.truffle.nodes.local;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzChunk;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzThread;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;

@NodeChildren({ @NodeChild("left"), @NodeChild("right"), @NodeChild("acc") })
public abstract class DFSEqualNode extends OzNode {

	public static DFSEqualNode create() {
		return DFSEqualNodeGen.create(null, null, null);
	}

	@Child DerefIfBoundNode derefIfBoundNode = DerefIfBoundNodeGen.create();

	public abstract boolean executeEqual(Object a, Object b, Object state);

	/**
	 * Put before recursive equality, to create state if it does not exist
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
	protected boolean equalRec(Object a, Object b, Object state) {
		return executeEqual(deref(a), deref(b), state);
	}

	@Specialization(guards = { "!isBound(a)", "!isBound(b)" })
	protected boolean equal(OzVar a, OzVar b, Object state) {
		a.makeNeeded();
		b.makeNeeded();
		while (!a.isLinkedTo(b) && !a.isBound() && !b.isBound()) {
			OzThread.getCurrent().yield(this);
		}
		if (a.isLinkedTo(b)) {
			return true;
		} else {
			return equalRec(a, b, state); // Either a or b is bound, at least one will be dereferenced
		}
	}

	@Specialization(guards = { "!isBound(a)", "!isVariable(b)" })
	protected Object equal(OzVar a, Object b, Object state) {
		return equalRec(a.waitValue(this), b, needState(state));
	}

	@Specialization(guards = { "!isVariable(a)", "!isBound(b)" })
	protected Object equal(Object a, OzVar b, Object state) {
		return equalRec(a, b.waitValue(this), needState(state));
	}

	@Specialization
	protected boolean equal(boolean a, boolean b, Object state) {
		return a == b;
	}

	@Specialization
	protected boolean equal(long a, long b, Object state) {
		return a == b;
	}

	@Specialization(guards = { "!isVariable(b)", "!isLong(b)", "!isBigInteger(b)" })
	protected boolean equal(long a, Object b, Object state) {
		return false;
	}

	@Specialization(guards = { "!isVariable(a)", "!isLong(a)", "!isBigInteger(a)" })
	protected boolean equal(Object a, long b, Object state) {
		return false;
	}

	@TruffleBoundary
	@Specialization
	protected boolean equal(BigInteger a, BigInteger b, Object state) {
		return a.equals(b);
	}

	@Specialization
	protected boolean equal(double a, double b, Object state) {
		return a == b;
	}

	@Specialization
	protected boolean equal(Unit a, Unit b, Object state) {
		return true;
	}

	@Specialization
	protected boolean equal(String a, String b, Object state) {
		return a == b;
	}

	@Specialization(guards = { "!isVariable(b)", "!isAtom(b)" })
	protected boolean equal(String a, Object b, Object state) {
		return false;
	}

	@Specialization(guards = { "!isVariable(a)", "!isAtom(a)" })
	protected boolean equal(Object a, String b, Object state) {
		return false;
	}

	@Specialization
	protected boolean equal(OzName a, OzName b, Object state) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzUniqueName a, OzUniqueName b, Object state) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzThread a, OzThread b, Object state) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzChunk a, OzChunk b, Object state) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzObject a, OzObject b, Object state) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzProc a, OzProc b, Object state) {
		return a.equals(b);
	}

	@TruffleBoundary
	@Specialization
	protected boolean equal(OzCons a, OzCons b, Object state) {
		Object newState = needState(state);
		return equalRec(a.getHead(), b.getHead(), newState) && equalRec(a.getTail(), b.getTail(), newState);
	}

	@TruffleBoundary
	@Specialization
	protected boolean equal(DynamicObject a, DynamicObject b, Object state) {
		if (a.getShape() != b.getShape()) {
			return false;
		}
		Object newState = needState(state);
		for (Property property : a.getShape().getProperties()) {
			Object aValue = property.get(a, a.getShape());
			Object bValue = property.get(b, b.getShape());
			if (!equalRec(aValue, bValue, newState)) {
				return false;
			}
		}
		return true;
	}

	@Specialization(guards = { "!isVariable(b)", "!isCons(b)", "!isRecord(b)" })
	protected boolean equal(OzCons a, Object b, Object state) {
		return false;
	}

	@Specialization(guards = { "!isVariable(b)", "!isRecord(b)" })
	protected boolean equal(DynamicObject record, Object b, Object state) {
		return false;
	}

	@Specialization(guards = { "a.getClass() != b.getClass()",
			"!isVariable(a)", "!isLong(a)", "!isBigInteger(a)", "!isAtom(a)", "!isCons(a)", "!isRecord(a)" })
	protected boolean equalRest(Object a, Object b, Object state) {
		return false;
	}

	protected Object deref(Object value) {
		return derefIfBoundNode.executeDerefIfBound(value);
	}

}
