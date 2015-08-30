package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class EqualNode extends OzNode {

	@Specialization
	protected boolean add(long a, long b) {
		return a == b;
	}

}
