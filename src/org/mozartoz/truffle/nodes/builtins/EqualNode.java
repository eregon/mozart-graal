package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class EqualNode extends OzNode {

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

}
