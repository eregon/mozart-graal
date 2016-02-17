package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class RecordBuiltins {

	@GenerateNodeFactory
	@NodeChild("record")
	public static abstract class LabelNode extends OzNode {

		@CreateCast("record")
		protected OzNode derefValue(OzNode value) {
			return DerefNodeGen.create(value);
		}

		@Specialization
		protected Object label(DynamicObject record) {
			return Arity.LABEL_LOCATION.get(record);
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("label"), @NodeChild("tuple") })
	public static abstract class MakeDynamicNode extends OzNode {

		@Specialization
		protected Object makeDynamicRecord(Object label, Object tuple) {
			return unit;
		}

	}

}
