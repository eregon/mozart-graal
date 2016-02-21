package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class TupleBuiltins {

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("label"), @NodeChild("width") })
	public static abstract class MakeNode extends OzNode {

		@CreateCast("width")
		protected OzNode derefWidth(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		Object make(Object label, long width) {
			if (width == 0) {
				return label;
			}
			Object[] features = new Object[(int) width];
			Object[] values = new Object[(int) width];
			for (int i = 0; i < features.length; i++) {
				features[i] = (long) (i + 1);
				values[i] = new OzVar();
			}
			Arity arity = Arity.build(label, features);
			return OzRecord.buildRecord(arity, values);
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
