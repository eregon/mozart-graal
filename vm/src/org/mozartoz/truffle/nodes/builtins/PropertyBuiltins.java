package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.PropertyRegistry;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class PropertyBuiltins {

	private static final PropertyRegistry REGISTRY = PropertyRegistry.INSTANCE;

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class RegisterValueNode extends OzNode {

		@Specialization
		Object registerValue(String property, Object value) {
			REGISTRY.registerValue(property, value);
			return unit;
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class RegisterConstantNode extends OzNode {

		@Specialization
		Object registerConstant(String property, Object value) {
			REGISTRY.registerConstant(property, value);
			return unit;
		}

	}

	@Builtin(deref = 1)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("result") })
	public static abstract class GetNode extends OzNode {

		@Specialization
		boolean get(String property, OzVar result) {
			Object value = REGISTRY.get(property);
			if (value != null) {
				result.bind(value);
				return true;
			} else {
				result.bind(unit);
				return false;
			}
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class PutNode extends OzNode {

		@Specialization
		boolean put(String property, Object value) {
			if (REGISTRY.containsKey(property)) {
				REGISTRY.put(property, value);
				return true;
			} else {
				return false;
			}
		}

	}

}
