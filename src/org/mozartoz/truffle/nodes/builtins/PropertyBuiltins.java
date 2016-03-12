package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.util.HashMap;
import java.util.Map;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class PropertyBuiltins {

	private static final Map<String, Object> PROPERTIES = new HashMap<>();

	static {
		PROPERTIES.put("platform.os", System.getProperty("os.name"));
		PROPERTIES.put("platform.name", System.getProperty("os.name"));

		PROPERTIES.put("oz.home", Loader.PROJECT_ROOT);
		PROPERTIES.put("oz.version", "3.0.0-alpha");
		PROPERTIES.put("oz.search.path", ".");
		PROPERTIES.put("oz.search.load", ".");

		PROPERTIES.put("limits.bytecode.xregisters", 65536L);
		PROPERTIES.put("limits.int.max", Long.MAX_VALUE);
		PROPERTIES.put("limits.int.min", Long.MIN_VALUE);
	}

	public static void setApplicationURL(String appURL) {
		PROPERTIES.put("application.url", appURL);
	}

	public static void setApplicationArgs(String[] args) {
		Object list = "nil";
		for (int i = args.length - 1; i >= 0; i--) {
			list = new OzCons(args[i].intern(), list);
		}
		PROPERTIES.put("application.args", list);
	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class RegisterValueNode extends OzNode {

		@Specialization
		Object registerValue(String property, Object value) {
			assert !PROPERTIES.containsKey(property);
			PROPERTIES.put(property, value);
			return unit;
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class RegisterConstantNode extends OzNode {

		@Specialization
		Object registerConstant(Object property, Object value) {
			return unimplemented();
		}

	}

	@Builtin(deref = 1)
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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class PutNode extends OzNode {

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