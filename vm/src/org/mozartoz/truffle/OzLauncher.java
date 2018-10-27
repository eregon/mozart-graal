package org.mozartoz.truffle;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.mozartoz.truffle.translator.Loader;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OzLauncher extends AbstractLanguageLauncher {

	private static final String BASE_TESTS = Loader.PROJECT_ROOT + "/platform-test/base/";
	private static final String TEST_RUNNER = Loader.PROJECT_ROOT + "/platform-test/simple_runner.oz";

	private static final String[] PASSING_TESTS = {
			BASE_TESTS + "int.oz",
			BASE_TESTS + "proc.oz",
			BASE_TESTS + "dictionary.oz",
			BASE_TESTS + "record.oz",
			BASE_TESTS + "state.oz",
			BASE_TESTS + "exception.oz",
			BASE_TESTS + "float.oz",
			BASE_TESTS + "conversion.oz",
			BASE_TESTS + "type.oz",
			BASE_TESTS + "byneed.oz",
			BASE_TESTS + "future.oz",
			BASE_TESTS + "tailrec.oz",
			BASE_TESTS + "unification.oz",
			BASE_TESTS + "onstack_clearing.oz",
	};

	public static void main(String[] args) {
		new OzLauncher().launch(args);
	}

	private String[] args = new String[0];

	@Override
	protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
		List<String> unrecognized = new ArrayList<>();

		for (int i = 0; i < arguments.size(); i++) {
			String argument = arguments.get(i);
			if (argument.startsWith("-")) {
				unrecognized.add(argument);
			} else {
				args = arguments.subList(i, arguments.size()).toArray(new String[0]);
				break;
			}
		}

		return unrecognized;
	}

	@Override
	protected void launch(Context.Builder contextBuilder) {
		final String functor;
		final String[] appArgs;
		if (args.length == 0) {
			functor = TEST_RUNNER;
			appArgs = PASSING_TESTS;
		} else {
			functor = args[0];
			appArgs = Arrays.copyOfRange(args, 1, args.length);
		}

		try (Context context = contextBuilder.arguments("oz", appArgs).build()) {
			context.eval(createSource(functor));
		}
	}

	@Override
	protected String getLanguageId() {
		return "oz";
	}

	@Override
	protected void printHelp(OptionCategory maxCategory) {
	}

	@Override
	protected void collectArguments(Set<String> options) {
	}

	private static Source createSource(String path) {
		File file = new File(path);
		try {
			return Source.newBuilder("oz", file).name(file.getName()).build();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
