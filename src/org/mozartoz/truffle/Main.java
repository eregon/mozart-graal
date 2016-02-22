package org.mozartoz.truffle;

import java.io.IOException;

import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.source.Source;

public class Main {

	public static void main(String[] args) throws IOException {
		Source source = Source.fromFileName("test.oz").withMimeType(OzLanguage.MIME_TYPE);
		Loader.getInstance().run(source);
	}

}
