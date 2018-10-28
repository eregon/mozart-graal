package org.mozartoz.truffle.translator;

import java.io.File;
import java.io.IOException;

import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class Loader {

	public static final Source MAIN_SOURCE = Source.newBuilder("oz", "", "main").internal(true).build();
	public static final SourceSection MAIN_SOURCE_SECTION = MAIN_SOURCE.createUnavailableSection();

	// Helpers

	public static Source createSource(Env env, String path) {
		File file = new File(path);
		try {
			return Source.newBuilder("oz", env.getTruffleFile(path)).name(file.getName()).build();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
