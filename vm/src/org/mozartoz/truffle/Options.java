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

	@Option(name = "free-slots", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> FREE_SLOTS = new OptionKey<>(true);

	@Option(name = "patvars-direct", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> DIRECT_PATTERN_VARS = new OptionKey<>(true);

	@Option(name = "vars.direct", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> DIRECT_VARS = new OptionKey<>(true);

	@Option(name = "vars.filtering", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> FRAME_FILTERING = new OptionKey<>(true);

	@Option(name = "reads-cache", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> CACHE_READ = new OptionKey<>(false);

	@Option(name = "split-builtins", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> SPLIT_BUILTINS = new OptionKey<>(true);

	@Option(name = "inline-cache-identity", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Integer> INLINE_CACHE_IDENTITY = new OptionKey<>(1);

	@Option(name = "inline-cache-calltarget", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Integer> INLINE_CACHE_CALLTARGET = new OptionKey<>(3);

	@Option(name = "inline-cache-method", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Integer> INLINE_CACHE_METHOD = new OptionKey<>(3);

	@Option(name = "cycles", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Boolean> CYCLE_DETECTION = new OptionKey<>(true);

	@Option(name = "cycles-threshold", help = "", category = USER, stability = STABLE)
	public static final OptionKey<Integer> CYCLE_THRESHOLD = new OptionKey<>(20);

	// Truffle options
	public static final int TruffleInvalidationReprofileCount = integer("graal.TruffleInvalidationReprofileCount", 3);
	public static final int TruffleOSRCompilationThreshold = integer("graal.TruffleOSRCompilationThreshold", 100_000);

	public static final boolean PRE_INITIALIZE_CONTEXTS = System.getProperty("polyglot.engine.PreinitializeContexts") != null;

	private static int integer(String property, int defaultValue) {
		return Integer.valueOf(System.getProperty(property, Integer.toString(defaultValue)));
	}

}
