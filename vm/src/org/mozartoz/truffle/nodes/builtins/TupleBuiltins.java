package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class TupleBuiltins {

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("label"), @NodeChild("width") })
	public static abstract class MakeNode extends OzNode {

		@Specialization
		Object make(Object label, Object width) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsTupleNode extends OzNode {

		@Specialization
		Object isTuple(Object value) {
			return unimplemented();
		}

	}

}
