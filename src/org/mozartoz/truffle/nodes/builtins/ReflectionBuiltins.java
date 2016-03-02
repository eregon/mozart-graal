package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class ReflectionBuiltins {

	@GenerateNodeFactory
	@NodeChild("stream")
	public static abstract class NewReflectiveEntityNode extends OzNode {

		@Specialization
		Object newReflectiveEntity(OzVar stream) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("stream")
	public static abstract class NewReflectiveVariableNode extends OzNode {

		@Specialization
		Object newReflectiveVariable(OzVar stream) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("variable")
	public static abstract class IsReflectiveVariableNode extends OzNode {

		@Specialization
		Object isReflectiveVariable(Object variable) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("variable"), @NodeChild("value") })
	public static abstract class BindReflectiveVariableNode extends OzNode {

		@Specialization
		Object bindReflectiveVariable(Object variable, Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("entity")
	public static abstract class GetStructuralBehaviorNode extends OzNode {

		@Specialization
		Object getStructuralBehavior(Object entity) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("entity"), @NodeChild("value") })
	public static abstract class BecomeNode extends OzNode {

		@Specialization
		Object become(Object entity, Object value) {
			return unimplemented();
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class ChangeRecordFieldNode extends OzNode {

		@Specialization
		Object changeRecordField(DynamicObject record, Object feature, Object newValue) {
			record.getShape().getProperty(feature).setInternal(record, newValue);
			return unit;
		}

	}

}
