package org.mozartoz.truffle;

import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.source.Source;

public class Main {

	private static final String BASE_TESTS = Loader.MOZART2_DIR + "/platform-test/base/";

	private static final String[] PASSING_TESTS = {
			BASE_TESTS + "int.oz",
			BASE_TESTS + "dictionary.oz",
			BASE_TESTS + "record.oz",
			BASE_TESTS + "state.oz",
			BASE_TESTS + "exception.oz",
			BASE_TESTS + "float.oz",
			BASE_TESTS + "conversion.oz",
			BASE_TESTS + "type.oz",
	};

	public static void main(String[] args) {
		// Source source = Loader.createSource("test.oz");
		// Loader.getInstance().run(source);

		Source source = Loader.createSource("simple_runner.oz");
		Loader.getInstance().runFunctor(source, PASSING_TESTS);
	}

}
