package org.mozartoz.truffle.nodes.builtins;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class EqualNode extends OzNode {

	public abstract boolean executeEqual(Object a, Object b);

	@Specialization
	protected boolean equal(boolean a, boolean b) {
		return a == b;
	}

	@Specialization
	protected boolean equal(long a, long b) {
		return a == b;
	}

	@Specialization(guards = "!isLong(b)")
	protected boolean equal(long a, Object b) {
		return false;
	}

	@Specialization(guards = "!isLong(a)")
	protected boolean equal(Object a, long b) {
		return false;
	}

	@Specialization
	protected boolean equal(BigInteger a, BigInteger b) {
		return a.equals(b);
	}

	@Specialization
	protected boolean equal(OzCons a, OzCons b) {
		return executeEqual(a.getHead(), b.getHead()) && executeEqual(a.getTail(), b.getTail());
	}

	@Specialization
	protected boolean equal(String a, String b) {
		return a == b;
	}

}
