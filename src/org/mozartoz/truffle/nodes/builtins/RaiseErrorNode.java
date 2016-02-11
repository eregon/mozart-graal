package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild("value")
public abstract class RaiseErrorNode extends OzNode {

	@Specialization
	protected Object raiseError(DynamicObject record) {
		throw new RuntimeException(record.toString());
	}

}