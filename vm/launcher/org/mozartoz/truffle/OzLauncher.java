package org.mozartoz.truffle;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OzLauncher extends AbstractLanguageLauncher {

	public static void main(String[] args) {
		new OzLauncher().launch(args);
	}

	private String[] args;

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
		debugPreInitializationOnJVM();

		final Source source = createSource(args[0]);
		final String[] appArgs = Arrays.copyOfRange(args, 1, args.length);

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

	private static void debugPreInitializationOnJVM() {
		if (!isAOT() && System.getProperty("polyglot.engine.PreinitializeContexts") != null) {
			try {
				final Class<?> holderClz = Class.forName("org.graalvm.polyglot.Engine$ImplHolder");
				final Method preInitMethod = holderClz.getDeclaredMethod("preInitializeEngine");
				preInitMethod.setAccessible(true);
				preInitMethod.invoke(null);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}

}
