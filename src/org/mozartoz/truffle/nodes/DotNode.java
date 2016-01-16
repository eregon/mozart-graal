package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("record"), @NodeChild("feature") })
public abstract class DotNode extends OzNode {

	@Specialization(guards = "feature == 1")
	protected Object getHead(OzCons cons, long feature) {
		return cons.getHead();
	}

	@Specialization(guards = "feature == 2")
	protected Object getTail(OzCons cons, long feature) {
		return cons.getTail();
	}

}
