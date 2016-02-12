package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("var"), @NodeChild("value") })
public abstract class BindVarValueNode extends OzNode {

	@Specialization(guards = "!isVar(value)")
	Object bindLeft(OzVar var, Object value) {
		var.bind(value);
		return unit;
	}

}
