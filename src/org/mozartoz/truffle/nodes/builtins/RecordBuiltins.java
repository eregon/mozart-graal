package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNode;
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
import com.oracle.truffle.api.object.DynamicObject;

public abstract class RecordBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsRecordNode extends OzNode {

		@Specialization
		Object isRecord(Object value) {
			return unimplemented();
		}

	}

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
	@NodeChild("record")
	public static abstract class WidthNode extends OzNode {

		@CreateCast("record")
		protected OzNode derefValue(OzNode value) {
			return DerefNodeGen.create(value);
		}

		@Specialization
		long width(DynamicObject record) {
			return OzRecord.getArity(record).getWidth();
		}

	}

	@GenerateNodeFactory
	@NodeChild("record")
	public static abstract class ArityNode extends OzNode {

		@CreateCast("record")
		protected OzNode derefValue(OzNode value) {
			return DerefNodeGen.create(value);
		}

		@Specialization
		Arity arity(String atom) {
			return Arity.forAtom(atom);
		}

		@Specialization
		Arity arity(DynamicObject record) {
			return OzRecord.getArity(record);
		}

	}

	@GenerateNodeFactory
	@NodeChild("record")
	public static abstract class CloneNode extends OzNode {

		@Specialization
		Object clone(Object record) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("record")
	public static abstract class WaitOrNode extends OzNode {

		@Specialization
		Object waitOr(Object record) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("label"), @NodeChild("contents") })
	public static abstract class MakeDynamicNode extends OzNode {

		@Child DerefNode derefNode = DerefNode.create();

		@CreateCast("label")
		protected OzNode derefLabel(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("contents")
		protected OzNode derefContents(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		protected String makeDynamic(String label, String emptyContents) {
			return label;
		}

		@Specialization
		protected DynamicObject makeDynamic(String label, DynamicObject contents) {
			int width = OzRecord.getArity(contents).getWidth();
			assert width % 2 == 0;
			int size = width / 2;
			Object[] features = new Object[size];
			Object[] values = new Object[size];
			for (int i = 0; i < size; i += 2) {
				features[i] = derefNode.executeDeref(contents.get((long) i + 1));
				values[i] = derefNode.executeDeref(contents.get((long) i + 2));
			}
			return OzRecord.buildRecord(Arity.build(label, features), values);
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("patLabel"), @NodeChild("patFeatures") })
	public static abstract class TestNode extends OzNode {

		@Specialization
		Object test(Object value, Object patLabel, Object patFeatures) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("patLabel") })
	public static abstract class TestLabelNode extends OzNode {

		@Specialization
		Object testLabel(Object value, Object patLabel) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("patFeature"), @NodeChild("found") })
	public static abstract class TestFeatureNode extends OzNode {

		@Specialization
		Object testFeature(Object value, Object patFeature, OzVar found) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature"), @NodeChild("fieldValue"), @NodeChild("result") })
	public static abstract class AdjoinAtIfHasFeatureNode extends OzNode {

		@Specialization
		Object adjoinAtIfHasFeature(Object record, Object feature, Object fieldValue, OzVar result) {
			return unimplemented();
		}

	}

}
