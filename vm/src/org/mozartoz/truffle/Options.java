package org.mozartoz.truffle;

public abstract class Options {

	public static final boolean MEASURE_STARTUP = System.getProperty("oz.measure.startup") != null;

	public static final String SHOW_PROC_AST = System.getProperty("oz.show.ast");

	public static final boolean PROFILER = bool("oz.profiler", false);

	public static final boolean SERIALIZER = !PROFILER && bool("oz.serializer", true);

	public static final boolean TAIL_CALLS = bool("oz.tail.calls", true);

	public static final boolean SELF_TAIL_CALLS = bool("oz.tail.selfcalls", true);
	public static final boolean SELF_TAIL_CALLS_OSR = bool("oz.tail.selfcalls.osr", true);

	public static final boolean STACKTRACE_ON_INTERRUPT = System.getProperty("oz.stacktrace.on_interrupt") != null;

	public static final boolean PRINT_NLINKS = bool("oz.print.nlinks", false);
	public static final boolean FREE_LINKS = bool("oz.free.links", true);

	// Truffle options
	public static final int TruffleInvalidationReprofileCount = integer("graal.TruffleInvalidationReprofileCount", 3);
	public static final int TruffleOSRCompilationThreshold = integer("graal.TruffleOSRCompilationThreshold", 100_000);

	private static boolean bool(String property, boolean defaultValue) {
		return Boolean.valueOf(System.getProperty(property, Boolean.toString(defaultValue)));
	}

	private static int integer(String property, int defaultValue) {
		return Integer.valueOf(System.getProperty(property, Integer.toString(defaultValue)));
	}

}
