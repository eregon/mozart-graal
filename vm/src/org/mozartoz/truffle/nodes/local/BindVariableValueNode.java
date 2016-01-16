package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChildren({ @NodeChild("var"), @NodeChild("value") })
public abstract class BindVariableValueNode extends OzNode {

	@Specialization
	public Object bind(VirtualFrame frame, OzVar var, Object value) {
		assert !(value instanceof OzVar);
		var.bind(value);
		return var;
	}

}
