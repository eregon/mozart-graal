package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class TupleBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("label"), @NodeChild("width") })
	public static abstract class MakeNode extends OzNode {

		@Specialization(guards = "isLiteral(label)")
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
			if (arity.isConsArity()) {
				return new OzCons(values[0], values[1]);
			} else {
				return OzRecord.buildRecord(arity, values);
			}
		}

	}

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsTupleNode extends OzNode {

		@Specialization
		boolean isTuple(String atom) {
			return true;
		}

		@Specialization
		boolean isTuple(OzName name) {
			return true;
		}

		@Specialization
		boolean isTuple(OzCons cons) {
			return true;
		}

		@Specialization
		boolean isTuple(DynamicObject record) {
			Arity arity = OzRecord.getArity(record);
			return arity.isTupleArity();
		}

	}

}
