package org.mozartoz.truffle.nodes.builtins;

import java.util.HashMap;
import java.util.Map;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class PropertyBuiltins {

	private static final Map<String, Object> PROPERTIES = new HashMap<>();

	static {
		PROPERTIES.put("platform.os", System.getProperty("os.name"));
	}

	@GenerateNodeFactory
	@NodeChild("property")
	public static abstract class RegisterValueNode extends OzNode {

		@Specialization
		Object registerValue(Object property) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("property")
	public static abstract class RegisterConstantNode extends OzNode {

		@Specialization
		Object registerConstant(Object property) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("result") })
	public static abstract class GetNode extends OzNode {

		@Specialization
		boolean get(String property, OzVar result) {
			if (PROPERTIES.containsKey(property)) {
				result.bind(PROPERTIES.get(property));
				return true;
			} else {
				result.bind(unit);
				return false;
			}
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class PutNode extends OzNode {

		@Specialization
		Object put(Object property, Object value) {
			return unimplemented();
		}

	}

}
