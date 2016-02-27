package org.mozartoz.truffle.nodes.builtins;

import java.util.HashMap;
import java.util.Map;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class PropertyBuiltins {

	private static final Map<String, Object> PROPERTIES = new HashMap<>();

	static {
		PROPERTIES.put("platform.os", System.getProperty("os.name"));
		PROPERTIES.put("oz.home", Loader.PROJECT_ROOT);
		PROPERTIES.put("oz.version", "3.0.0-alpha");
		PROPERTIES.put("oz.search.path", ".");
		PROPERTIES.put("oz.search.load", ".");
	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class RegisterValueNode extends OzNode {

		@CreateCast("property")
		protected OzNode derefProperty(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("value")
		protected OzNode derefValue(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		Object registerValue(String property, Object value) {
			assert !PROPERTIES.containsKey(property);
			PROPERTIES.put(property, value);
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class RegisterConstantNode extends OzNode {

		@Specialization
		Object registerConstant(Object property, Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("result") })
	public static abstract class GetNode extends OzNode {

		@CreateCast("property")
		protected OzNode derefProperty(OzNode var) {
			return DerefNodeGen.create(var);
		}

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

		@CreateCast("property")
		protected OzNode derefProperty(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("value")
		protected OzNode derefValue(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		boolean put(String property, Object value) {
			if (PROPERTIES.containsKey(property)) {
				PROPERTIES.put(property, value);
				return true;
			} else {
				return false;
			}
		}

	}

}
