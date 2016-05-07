package org.mozartoz.truffle;

import java.util.Arrays;

import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.source.Source;

public class Main {

	private static final String BASE_TESTS = Loader.MOZART2_DIR + "/platform-test/base/";
	private static final String TEST_RUNNER = Loader.MOZART2_DIR + "/platform-test/simple_runner.oz";

	private static final String[] PASSING_TESTS = {
			BASE_TESTS + "int.oz",
			BASE_TESTS + "dictionary.oz",
			BASE_TESTS + "record.oz",
			BASE_TESTS + "state.oz",
			BASE_TESTS + "exception.oz",
			BASE_TESTS + "float.oz",
			BASE_TESTS + "conversion.oz",
			BASE_TESTS + "type.oz",
			BASE_TESTS + "byneed.oz",
			BASE_TESTS + "future.oz",
	};

	public static void main(String[] args) {
		// Source source = Loader.createSource("test.oz");
		// Loader.getInstance().run(source);

		if (args.length == 0) {
			Source source = Loader.createSource(TEST_RUNNER);
			Loader.getInstance().runFunctor(source, PASSING_TESTS);
		} else {
			String functor = args[0];
			Source source = Loader.createSource(functor);
			String[] appArgs = Arrays.copyOfRange(args, 1, args.length);
			Loader.getInstance().runFunctor(source, appArgs);
		}
	}

}
