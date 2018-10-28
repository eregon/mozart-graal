package org.mozartoz.truffle;

import com.oracle.truffle.api.TruffleOptions;

public abstract class Options {

	public static final boolean MEASURE_STARTUP = bool("oz.measure.startup", false);

	public static final String SHOW_PROC_AST = System.getProperty("oz.show.ast");

	public static final boolean SERIALIZER = bool("oz.serializer", !TruffleOptions.AOT);

	public static final boolean TAIL_CALLS = bool("oz.tail.calls", true);
	public static final boolean TAIL_CALLS_OSR = bool("oz.tail.calls.osr", true);

	public static final boolean SELF_TAIL_CALLS = bool("oz.tail.selfcalls", true);
	public static final boolean SELF_TAIL_CALLS_OSR = bool("oz.tail.selfcalls.osr", true);

	public static final boolean STACKTRACE_ON_INTERRUPT = bool("oz.stacktrace.on_interrupt", false);

	public static final boolean FREE_LINKS = bool("oz.free.links", true);
	public static final boolean FREE_SLOTS = bool("oz.free.slots", true);
	public static final boolean DIRECT_PATTERN_VARS = bool("oz.patvars.direct", true);
	public static final boolean DIRECT_VARS = bool("oz.vars.direct", true);
	public static final boolean FRAME_FILTERING = bool("oz.vars.filtering", true);
	public static final boolean CACHE_READ = bool("oz.reads.cache", false);
	public static final boolean PRINT_NLINKS = bool("oz.print.nlinks", false);
	public static final boolean PRINT_NVARS = bool("oz.print.nvars", false);

	public static final boolean SPLIT_BUILTINS = bool("oz.builtins.split", true);

	public static final int INLINE_FRAMES = integer("oz.frames.cache", 1);
	public static final int INLINE_CALLTARGET = integer("oz.calltargets.cache", 3);
	public static final boolean OPTIMIZE_METHODS = bool("oz.methods.cache", true);

	public static final boolean CYCLE_DETECTION = bool("oz.cycles", true);
	public static final int CYCLE_THRESHOLD = integer("oz.cycles.threshold", 20);

	// Truffle options
	public static final int TruffleInvalidationReprofileCount = integer("graal.TruffleInvalidationReprofileCount", 3);
	public static final int TruffleOSRCompilationThreshold = integer("graal.TruffleOSRCompilationThreshold", 100_000);

	private static boolean bool(String property, boolean defaultValue) {
		String value = System.getProperty(property, Boolean.toString(defaultValue));
		if (value.equalsIgnoreCase("true")) {
			return true;
		} else if (value.equalsIgnoreCase("false")) {
			return false;
		}
		System.err.println(property + " was expected to be true or false, got '" + value + "'");
		System.exit(1);
		return false;
	}

	private static int integer(String property, int defaultValue) {
		return Integer.valueOf(System.getProperty(property, Integer.toString(defaultValue)));
	}

}
