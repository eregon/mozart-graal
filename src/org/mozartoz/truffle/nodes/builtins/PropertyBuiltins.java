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
		PROPERTIES.put("platform.os", System.getProperty("os.name").intern());
		PROPERTIES.put("platform.name", System.getProperty("os.name").intern());

		PROPERTIES.put("oz.home", Loader.PROJECT_ROOT.intern());
		PROPERTIES.put("oz.version", "3.0.0-alpha");
		PROPERTIES.put("oz.search.path", ".");
		PROPERTIES.put("oz.search.load", ("cache=/usr/share/mozart/cache").intern());

		PROPERTIES.put("limits.bytecode.xregisters", 65536L);
		PROPERTIES.put("limits.int.max", Long.MAX_VALUE);
		PROPERTIES.put("limits.int.min", Long.MIN_VALUE);

		PROPERTIES.put("application.gui", false);

		PROPERTIES.put("errors.debug", true);
		PROPERTIES.put("errors.depth", 10L);
		PROPERTIES.put("errors.width", 20L);
		PROPERTIES.put("errors.thread", 40L);

		PROPERTIES.put("priorities.high", 10L);
		PROPERTIES.put("priorities.medium", 10L);

		PROPERTIES.put("gc.active", 0L);
		PROPERTIES.put("gc.codeCycles", 0L);
		PROPERTIES.put("gc.free", 75L);
		PROPERTIES.put("gc.on", true);
		PROPERTIES.put("gc.tolerance", 10L);
		long MB = 1024L * 1024L;
		PROPERTIES.put("gc.min", 16L * MB);
		PROPERTIES.put("gc.size", 32L * MB);
		PROPERTIES.put("gc.threshold", 48L * MB);

		PROPERTIES.put("messages.gc", false);
		PROPERTIES.put("messages.idle", false);

		PROPERTIES.put("threads.created", 1L);
		PROPERTIES.put("threads.runnable", 1L);
		PROPERTIES.put("threads.min", 1L);

		PROPERTIES.put("time.user", 0L);
		PROPERTIES.put("time.system", 0L);
		PROPERTIES.put("time.total", 0L);
		PROPERTIES.put("time.run", 0L);
		PROPERTIES.put("time.idle", 0L);
		PROPERTIES.put("time.copy", 0L);
		PROPERTIES.put("time.propagate", 0L);
		PROPERTIES.put("time.gc", 0L);
		PROPERTIES.put("time.detailed", false);
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
