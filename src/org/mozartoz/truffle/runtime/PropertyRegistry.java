package org.mozartoz.truffle.runtime;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class PropertyRegistry {

	public static final PropertyRegistry INSTANCE = new PropertyRegistry();
	private static final long START_TIME = System.currentTimeMillis();

	private final Map<String, Accessor> properties = new HashMap<>();

	private PropertyRegistry() {
	}

	public void initialize() {
		registerValue("oz.home", Loader.PROJECT_ROOT.intern());
		registerConstant("oz.version", "3.0.0-alpha");
		registerValue("oz.search.path", ".");
		registerValue("oz.search.load", ("cache=" + Loader.LOCAL_LIB_DIR).intern());

		registerConstant("application.gui", false);

		registerValue("errors.debug", true);
		registerValue("errors.thread", 40L);
		registerValue("errors.width", 20L);
		registerValue("errors.depth", 10L);

		registerConstant("fd.variables", 0L);
		registerConstant("fd.propagators", 0L);
		registerConstant("fd.invoked", 0L);
		registerValue("fd.threshold", 0L);

		registerComputed("gc.active", () -> GarbageCollectionNotifier.getLastActiveSize());
		registerValue("gc.codeCycles", 0L);
		registerValue("gc.free", 75L);
		registerValue("gc.on", true);
		registerValue("gc.tolerance", 10L);
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		registerValue("gc.min", memoryMXBean.getHeapMemoryUsage().getInit());
		registerComputed("gc.size", () -> memoryMXBean.getHeapMemoryUsage().getUsed());
		registerComputed("gc.threshold", () -> memoryMXBean.getHeapMemoryUsage().getCommitted());

		registerConstant("limits.bytecode.xregisters", 65536L);
		registerConstant("limits.int.max", Long.MAX_VALUE);
		registerConstant("limits.int.min", Long.MIN_VALUE);

		registerValue("messages.gc", false);
		registerValue("messages.idle", false);

		registerConstant("platform.os", System.getProperty("os.name").intern());
		registerConstant("platform.name", System.getProperty("os.name").intern());

		registerValue("priorities.high", 10L);
		registerValue("priorities.medium", 10L);

		registerConstant("spaces.created", 0L);
		registerConstant("spaces.cloned", 0L);
		registerConstant("spaces.committed", 0L);
		registerConstant("spaces.failed", 0L);
		registerConstant("spaces.succeeded", 0L);

		registerComputed("threads.created", () -> OzThread.getNumberOfThreadsCreated());
		registerComputed("threads.runnable", () -> OzThread.getNumberOfThreadsRunnable());
		registerConstant("threads.min", 1L);

		registerValue("print.verbose", false);
		registerValue("print.width", 20L);
		registerValue("print.depth", 10L);

		registerConstant("time.user", 0L);
		registerConstant("time.system", 0L);
		registerConstant("time.total", 0L);
		registerComputed("time.run", () -> System.currentTimeMillis() - START_TIME);
		registerConstant("time.idle", 0L);
		registerConstant("time.copy", 0L);
		registerConstant("time.propagate", 0L);
		registerComputed("time.gc", () -> {
			long total = 0L;
			for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
				total += gcBean.getCollectionTime();
			}
			return total;
		});
		registerValue("time.detailed", false);

		GarbageCollectionNotifier.register();
	}

	@TruffleBoundary
	public boolean containsKey(String property) {
		return properties.containsKey(property);
	}

	@TruffleBoundary
	public Object get(String property) {
		Accessor accessor = properties.get(property);
		if (accessor == null) {
			return null;
		}
		return accessor.get();
	}

	@TruffleBoundary
	public void put(String property, Object value) {
		assert isOzValue(value) : value;
		Accessor accessor = properties.get(property);
		accessor.set(value);
	}

	@TruffleBoundary
	public void registerConstant(String property, Object value) {
		assert isOzValue(value) : value;
		registerInternal(property, new ConstantAccessor(property, value));
	}

	@TruffleBoundary
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
