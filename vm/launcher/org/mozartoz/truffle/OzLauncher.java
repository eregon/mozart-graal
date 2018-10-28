package org.mozartoz.truffle;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OzLauncher extends AbstractLanguageLauncher {

	public static void main(String[] args) {
		new OzLauncher().launch(args);
	}

	private String[] args = new String[0];

	@Override
	protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
		List<String> unrecognized = new ArrayList<>();

		for (int i = 0; i < arguments.size(); i++) {
			String argument = arguments.get(i);
			if (argument.startsWith("-")) {
				unrecognized.add(argument);
			} else {
				args = arguments.subList(i, arguments.size()).toArray(new String[0]);
				break;
			}
		}

		return unrecognized;
	}

	@Override
	protected void launch(Context.Builder contextBuilder) {
		final boolean runTests = args.length == 0;
		final Source source;
		final String[] appArgs;
		if (runTests) {
			source = Source.create("oz", "RUN_TESTS");
			appArgs = new String[0];
		} else {
			source = createSource(args[0]);
			appArgs = Arrays.copyOfRange(args, 1, args.length);
		}

		try (Context context = contextBuilder.arguments("oz", appArgs).build()) {
			context.eval(source);
		}
	}

	@Override
	protected String getLanguageId() {
		return "oz";
	}

	@Override
	protected void printHelp(OptionCategory maxCategory) {
	}

	@Override
	protected void collectArguments(Set<String> options) {
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
