package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("label"), @NodeChild("tuple") })
public abstract class RecordMakeDynamicNode extends OzNode {

	@Specialization
	protected Object makeDynamicRecord(Object label, Object tuple) {
		return unit;
	}


}
