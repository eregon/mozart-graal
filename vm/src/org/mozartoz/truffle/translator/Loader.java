package org.mozartoz.truffle.translator;

import java.io.File;
import java.io.IOException;

import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class Loader {

	public static final String PROJECT_ROOT = getProjectRoot();
	public static final String LIB_DIR = PROJECT_ROOT + "/lib";
	public static final String MAIN_LIB_DIR = LIB_DIR + "/main";

	static final String TOOLS_DIR = LIB_DIR + "/tools";
	public static final String BASE_FILE_NAME = MAIN_LIB_DIR + "/base/Base.oz";
	public static final String INIT_FUNCTOR = MAIN_LIB_DIR + "/init/Init.oz";

	public static final String MAIN_IMAGE = PROJECT_ROOT + "/Main.image";

	public static final String OZWISH = PROJECT_ROOT + "/wish/ozwish";

	public static final String[] SYSTEM_LOAD_PATH = new String[] {
			MAIN_LIB_DIR + "/sys",
			MAIN_LIB_DIR + "/support",
			MAIN_LIB_DIR + "/sp",
			MAIN_LIB_DIR + "/op",
			MAIN_LIB_DIR + "/cp",
			MAIN_LIB_DIR + "/ap",
			MAIN_LIB_DIR + "/wp",
			TOOLS_DIR + "/panel",
			TOOLS_DIR + "/browser",
			LIB_DIR + "/compiler",
			PROJECT_ROOT + "/stdlib/wp/qtk",
	};
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

	private static String getProjectRoot() {
		String thisFile = Loader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = new File(thisFile).getParentFile().getParent();
		return path;
	}

}
