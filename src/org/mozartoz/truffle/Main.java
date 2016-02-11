package org.mozartoz.truffle;

import java.io.File;
import java.io.IOException;

import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.translator.Translator;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;

public class Main {

	public static final PolyglotEngine ENGINE = PolyglotEngine.newBuilder().build();

	public static final String PROJECT_ROOT = getProjectRoot();

	public static void main(String[] args) throws IOException {
		Source source = Source.fromFileName(PROJECT_ROOT + "/test.oz");
		eval(source.getCode());
	}

	private static void eval(String code) {
		// ENGINE.eval(Source.fromText(code, "test.oz").withMimeType(OzLanguage.MIME_TYPE));
		parseAndExecute(code);
	}

	public static void parseAndExecute(String code) {
		execute(new Translator().parseAndTranslate(code));
	}

	private static Object execute(OzRootNode rootNode) {
		return Truffle.getRuntime().createCallTarget(rootNode).call();
	}

	private static String getProjectRoot() {
		String thisFile = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = new File(thisFile).getParent();
		return path;
	}

}
