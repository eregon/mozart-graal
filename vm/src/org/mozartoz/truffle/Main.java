package org.mozartoz.truffle;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.mozartoz.truffle.translator.Loader;

public class Main {

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
		final String[] appArgs;
		if (args.length == 0) {
			appArgs = PASSING_TESTS;
		} else {
			appArgs = Arrays.copyOfRange(args, 1, args.length);
		}

		try (Context context = Context.newBuilder().allowAllAccess(true).arguments("oz", appArgs).build()) {
			if (args.length == 0) {
				context.eval(createSource(TEST_RUNNER));
			} else {
				String functor = args[0];
				context.eval(createSource(functor));
			}
		}

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
