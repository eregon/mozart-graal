package org.mozartoz.truffle;

public abstract class Options {

	public static final boolean MEASURE_STARTUP = System.getProperty("oz.measure.startup") != null;

	public static final String SHOW_PROC_AST = System.getProperty("oz.show.ast");

	public static final boolean PROFILER = bool("oz.profiler", false);

	public static final boolean SERIALIZER = !PROFILER && bool("oz.serializer", true);

	public static final boolean TAIL_CALLS = bool("oz.tail.calls", true);

	public static final boolean STACKTRACE_ON_INTERRUPT = System.getProperty("oz.stacktrace.on_interrupt") != null;

	private static boolean bool(String property, boolean defaultValue) {
		return Boolean.valueOf(System.getProperty(property, Boolean.toString(defaultValue)));
	}

}
