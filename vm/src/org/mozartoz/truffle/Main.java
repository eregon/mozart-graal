package org.mozartoz.truffle;

import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.source.Source;

public class Main {

	public static void main(String[] args) {
		Source source = Loader.createSource("test.oz");
		Loader.getInstance().run(source);
	}

}
