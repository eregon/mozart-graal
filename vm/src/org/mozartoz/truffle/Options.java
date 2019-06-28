package org.mozartoz.truffle;

import com.oracle.truffle.api.Option;
import com.oracle.truffle.api.TruffleOptions;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionType;

import static org.graalvm.options.OptionCategory.USER;
import static org.graalvm.options.OptionStability.STABLE;

@Option.Group("oz")
public abstract class Options {

	public static final OptionDescriptors DESCRIPTORS = new OptionsOptionDescriptors();

	@Option(name = "measure-startup", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> MEASURE_STARTUP = new OptionKey<>(false);

	@Option(name = "show-ast", help = "", category = USER, stability = STABLE)
	public static final OptionKey<String> SHOW_AST = new OptionKey<>(null, OptionType.defaultType(""));

	@Option(name = "serializer", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> SERIALIZER = new OptionKey<>(!TruffleOptions.AOT);

	@Option(name = "tail-calls", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> TAIL_CALLS = new OptionKey<>(true);

	@Option(name = "tail-calls-osr", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> TAIL_CALLS_OSR = new OptionKey<>(true);

	@Option(name = "tail-selfcalls", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> SELF_TAIL_CALLS = new OptionKey<>(true);

	@Option(name = "tail-selfcalls-osr", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> SELF_TAIL_CALLS_OSR = new OptionKey<>(true);

	@Option(name = "stacktrace-on-interrupt", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> STACKTRACE_ON_INTERRUPT = new OptionKey<>(false);

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

	public static final boolean PRE_INITIALIZE_CONTEXTS = System.getProperty("polyglot.engine.PreinitializeContexts") != null;

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
