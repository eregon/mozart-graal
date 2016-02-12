package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.EqualNode;
import org.mozartoz.truffle.nodes.builtins.EqualNodeGen;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class UnifyNode extends OzNode {

	// TODO: should undo bindings if unification fails.
	// Maybe by catching the failed unification exception?

	@Child EqualNode equalNode;

	public abstract Object executeUnify(Object a, Object b);

	@Specialization(guards = { "!isVar(a)", "!isBound(b)" })
	Object unify(Object a, OzVar b) {
		b.bind(a);
		return unit;
	}

	@Specialization
	Object unify(OzCons a, OzCons b) {
		executeUnify(a.getHead(), b.getHead());
		executeUnify(a.getTail(), b.getTail());
		return unit;
	}

	@Specialization(guards = { "!isVar(a)", "!isVar(b)", "!isCons(a)", "!isCons(b)" })
	Object unify(Object a, Object b) {
		if (!equal(a, b)) {
			CompilerDirectives.transferToInterpreter();
			throw new RuntimeException("Failed unification: " + a + " != " + b);
		}
		return unit;
	}

	private boolean equal(Object a, Object b) {
		if (equalNode == null) {
			CompilerDirectives.transferToInterpreter();
			equalNode = insert(EqualNodeGen.create(null, null));
		}
		return equalNode.executeEqual(a, b);
	}

}
