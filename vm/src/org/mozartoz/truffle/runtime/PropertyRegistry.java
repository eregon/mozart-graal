package org.mozartoz.truffle.runtime;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.translator.Loader;

public class PropertyRegistry {

	public static final PropertyRegistry INSTANCE = new PropertyRegistry();

	private final Map<String, Accessor> properties = new HashMap<>();

	private PropertyRegistry() {
	}

	public void initialize() {
		registerValue("oz.home", Loader.PROJECT_ROOT.intern());
		registerConstant("oz.version", "3.0.0-alpha");
		registerValue("oz.search.path", ".");
		registerValue("oz.search.load", ("cache=/usr/share/mozart/cache").intern());

		registerConstant("application.gui", false);

		registerConstant("errors.debug", true);
		registerConstant("errors.depth", 10L);
		registerConstant("errors.width", 20L);
		registerConstant("errors.thread", 40L);

		registerConstant("fd.variables", 0L);
		registerConstant("fd.propagators", 0L);
		registerConstant("fd.invoked", 0L);
		registerValue("fd.threshold", 0L);

		registerConstant("gc.active", 0L);
		registerConstant("gc.codeCycles", 0L);
		registerConstant("gc.free", 75L);
		registerConstant("gc.on", true);
		registerConstant("gc.tolerance", 10L);
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		registerComputed("gc.min", () -> {
			return memoryMXBean.getHeapMemoryUsage().getInit();
		});
		registerComputed("gc.size", () -> {
			return memoryMXBean.getHeapMemoryUsage().getUsed();
		});
		registerComputed("gc.threshold", () -> {
			return memoryMXBean.getHeapMemoryUsage().getCommitted();
		});

		registerConstant("limits.bytecode.xregisters", 65536L);
		registerConstant("limits.int.max", Long.MAX_VALUE);
		registerConstant("limits.int.min", Long.MIN_VALUE);

		registerConstant("messages.gc", false);
		registerConstant("messages.idle", false);

		registerConstant("platform.os", System.getProperty("os.name").intern());
		registerConstant("platform.name", System.getProperty("os.name").intern());

		registerConstant("priorities.high", 10L);
		registerConstant("priorities.medium", 10L);

		registerConstant("spaces.created", 0L);
		registerConstant("spaces.cloned", 0L);
		registerConstant("spaces.committed", 0L);
		registerConstant("spaces.failed", 0L);
		registerConstant("spaces.succeeded", 0L);

		registerConstant("threads.created", 1L);
		registerConstant("threads.runnable", 1L);
		registerConstant("threads.min", 1L);

		registerConstant("time.user", 0L);
		registerConstant("time.system", 0L);
		registerConstant("time.total", 0L);
		registerConstant("time.run", 0L);
		registerConstant("time.idle", 0L);
		registerConstant("time.copy", 0L);
		registerConstant("time.propagate", 0L);
		registerConstant("time.gc", 0L);
		registerValue("time.detailed", false);
	}

	public boolean containsKey(String property) {
		return properties.containsKey(property);
	}

	public Object get(String property) {
		Accessor accessor = properties.get(property);
		if (accessor == null) {
			return null;
		}
		return accessor.get();
	}

	public void put(String property, Object value) {
		assert isOzValue(value) : value;
		Accessor accessor = properties.get(property);
		accessor.set(value);
	}

	public void registerConstant(String property, Object value) {
		assert isOzValue(value) : value;
		registerInternal(property, new ConstantAccessor(property, value));
	}

	public void registerValue(String property, Object value) {
		assert isOzValue(value) : value;
		registerInternal(property, new ValueAccessor(value));
	}

	public void registerComputed(String property, Supplier<Object> getter) {
		registerInternal(property, new ComputedAccessor(property, getter));
	}

	private void registerInternal(String property, Accessor accessor) {
		if (properties.containsKey(property)) {
			throw new RuntimeException();
		}
		properties.put(property, accessor);
	}

	private static boolean isOzValue(Object value) {
		return OzGuards.isFeature(value) || OzGuards.isFloat(value) ||
				OzGuards.isCons(value) || OzGuards.isRecord(value) ||
				OzGuards.isProc(value);
	}

	public void setApplicationURL(String appURL) {
		registerConstant("application.url", appURL);
	}

	public void setApplicationArgs(String[] args) {
		Object list = "nil";
		for (int i = args.length - 1; i >= 0; i--) {
			list = new OzCons(args[i].intern(), list);
		}
		registerConstant("application.args", list);
	}

	public interface Accessor {

		Object get();

		void set(Object newValue);

	}

	protected class ConstantAccessor implements Accessor {

		private final String property;
		private final Object value;

		public ConstantAccessor(String property, Object value) {
			this.property = property;
			this.value = value;
		}

		@Override
		public Object get() {
			return value;
		}

		@Override
		public void set(Object newValue) {
			throw new RuntimeException("Tried to set constant property " + property);
		}

	}

	protected class ValueAccessor implements Accessor {

		private Object value;

		public ValueAccessor(Object value) {
			this.value = value;
		}

		@Override
		public Object get() {
			return value;
		}

		@Override
		public void set(Object newValue) {
			this.value = newValue;
		}

	}

	protected class ComputedAccessor implements Accessor {

		private final String property;
		private final Supplier<Object> getter;

		public ComputedAccessor(String property, Supplier<Object> getter) {
			this.property = property;
			this.getter = getter;
		}

		@Override
		public Object get() {
			Object value = getter.get();
			assert isOzValue(value);
			return value;
		}

		@Override
		public void set(Object newValue) {
			throw new RuntimeException("Tried to set computed property " + property);
		}

	}

}
