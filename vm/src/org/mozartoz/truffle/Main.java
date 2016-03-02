package org.mozartoz.truffle;

import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.source.Source;

public class Main {

	public static void main(String[] args) {
		// Source source = Loader.createSource("test.oz");
		// Loader.getInstance().run(source);

		Source source = Loader.createSource("simple_runner.oz");
		String BASE_TESTS = Loader.MOZART2_DIR + "/platform-test/base/";
		Loader.getInstance().runFunctor(source,
				BASE_TESTS + "int.oz",
				BASE_TESTS + "dictionary.oz",
				BASE_TESTS + "record.oz");
	}

}
