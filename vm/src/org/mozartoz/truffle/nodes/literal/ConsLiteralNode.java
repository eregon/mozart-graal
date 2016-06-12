package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("head"), @NodeChild("tail") })
public abstract class ConsLiteralNode extends OzNode {

	@CreateCast("head")
	protected OzNode derefHead(OzNode var) {
		return DerefIfBoundNodeGen.create(var);
	}

	@CreateCast("tail")
	protected OzNode derefTail(OzNode var) {
		return DerefIfBoundNodeGen.create(var);
	}

	public abstract OzNode getHead();

	public abstract OzNode getTail();

	@Specialization
	public Object execute(Object head, Object tail) {
		return new OzCons(head, tail);
	}

}
