package org.mozartoz.truffle.nodes.local;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzChunk;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzException;
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

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class DFSEqualNode extends OzNode {

	public static DFSEqualNode create() {
		return DFSEqualNodeGen.create(null, null);
	}

	@Child DerefIfBoundNode derefIfBoundNode = DerefIfBoundNodeGen.create();

	public abstract boolean executeEqual(Object a, Object b);

	@TruffleBoundary
	protected boolean equalRec(Object a, Object b) {
		return executeEqual(deref(a), deref(b));
	}

	@Specialization(guards = { "!isBound(a)", "!isBound(b)" })
	protected boolean equal(OzVar a, OzVar b) {
		while (!a.isLinkedTo(b) && !a.isBound() && !b.isBound()) {
			a.makeNeeded();
			b.makeNeeded();
			OzThread.getCurrent().yield(this);
		}
		if (a.isLinkedTo(b)) {
			return true;
		} else {
			throw new OzException(this, "unimplemented");
		}
	}

	@Specialization(guards = { "!isBound(a)", "!isVariable(b)" })
	protected Object equal(OzVar a, Object b) {
		return equalRec(a.waitValue(this), b);
	}

	@Specialization(guards = { "!isVariable(a)", "!isBound(b)" })
	protected Object equal(Object a, OzVar b) {
		return equalRec(a, b.waitValue(this));
	}

	@Specialization
	protected boolean equal(boolean a, boolean b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(long a, long b) {
		return a == b;
	}

	@Specialization(guards = { "!isVariable(b)", "!isLong(b)", "!isBigInteger(b)" })
	protected boolean equal(long a, Object b) {
		return false;
	}

	@Specialization(guards = { "!isVariable(a)", "!isLong(a)", "!isBigInteger(a)" })
	protected boolean equal(Object a, long b) {
		return false;
	}

	@TruffleBoundary
	@Specialization
	protected boolean equal(BigInteger a, BigInteger b) {
		return a.equals(b);
	}

	@Specialization
	protected boolean equal(double a, double b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(Unit a, Unit b) {
		return true;
	}

	@Specialization
	protected boolean equal(String a, String b) {
		return a == b;
	}

	@Specialization(guards = { "!isVariable(b)", "!isAtom(b)" })
	protected boolean equal(String a, Object b) {
		return false;
	}

	@Specialization(guards = { "!isVariable(a)", "!isAtom(a)" })
	protected boolean equal(Object a, String b) {
		return false;
	}

	@Specialization
	protected boolean equal(OzName a, OzName b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzUniqueName a, OzUniqueName b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzThread a, OzThread b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzChunk a, OzChunk b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzObject a, OzObject b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(OzProc a, OzProc b) {
		return a.equals(b);
	}

	@TruffleBoundary
	@Specialization
	protected boolean equal(OzCons a, OzCons b) {
		return equalRec(a.getHead(), b.getHead()) && equalRec(a.getTail(), b.getTail());
	}

	@TruffleBoundary
	@Specialization
	protected boolean equal(DynamicObject a, DynamicObject b) {
		if (a.getShape() != b.getShape()) {
			return false;
		}
		for (Property property : a.getShape().getProperties()) {
			Object aValue = property.get(a, a.getShape());
			Object bValue = property.get(b, b.getShape());
			if (!equalRec(aValue, bValue)) {
				return false;
			}
		}
		return true;
	}

	@Specialization(guards = { "!isVariable(b)", "!isCons(b)", "!isRecord(b)" })
	protected boolean equal(OzCons a, Object b) {
		return false;
	}

	@Specialization(guards = { "!isVariable(b)", "!isRecord(b)" })
	protected boolean equal(DynamicObject record, Object b) {
		return false;
	}

	@Specialization(guards = { "a.getClass() != b.getClass()",
			"!isVariable(a)", "!isLong(a)", "!isBigInteger(a)", "!isAtom(a)", "!isCons(a)", "!isRecord(a)" })
	protected boolean equalRest(Object a, Object b) {
		return false;
	}

	protected Object deref(Object value) {
		return derefIfBoundNode.executeDerefIfBound(value);
	}

}
