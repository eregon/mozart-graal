package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild("record")
public abstract class LabelNode extends OzNode {

	@CreateCast("record")
	protected OzNode derefValue(OzNode value) {
		return DerefNodeGen.create(value);
	}

	@Specialization
	protected Object not(DynamicObject record) {
		return Arity.LABEL_LOCATION.get(record);
	}

}
