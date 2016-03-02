package org.mozartoz.truffle;

import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.source.Source;

public class Main {

	public static void main(String[] args) {
		// Source source = Loader.createSource("test.oz");
		// Loader.getInstance().run(source);

		Source source = Loader.createSource("simple_runner.oz");
		Loader.getInstance().runFunctor(source, Loader.MOZART2_DIR + "/platform-test/base/int.oz");
	}

}
