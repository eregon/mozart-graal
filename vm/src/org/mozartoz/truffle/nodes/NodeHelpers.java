package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public abstract class NodeHelpers {

	@ExplodeLoop
	public static Object[] executeValues(VirtualFrame frame, OzNode[] values) {
		Object[] array = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			array[i] = values[i].execute(frame);
		}
		return array;
	}

	public static OzNode[] deref(OzNode[] values) {
		OzNode[] deref = new OzNode[values.length];
		for (int i = 0; i < values.length; i++) {
			deref[i] = DerefNode.create(values[i]);
		}
		return deref;
	}

	public static OzNode[] derefIfBound(OzNode[] values) {
		OzNode[] deref = new OzNode[values.length];
		for (int i = 0; i < values.length; i++) {
			deref[i] = DerefIfBoundNode.create(values[i]);
		}
		return deref;
	}

}
