package org.mozartoz.truffle.runtime;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.translator.ApplyFunctor;
import org.mozartoz.truffle.translator.Loader;
import org.mozartoz.truffle.translator.OzSerializer;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class OzContext {

	static OzContext instance = null;

	public static OzContext getInstance() {
		return instance;
	}

	private final Env env;
	private final OzLanguage language;
	private final String home;

	private final TranslatorDriver translatorDriver;
	private final PropertyRegistry propertyRegistry;
	private final File mainImage;

	private final List<Process> childProcesses = new LinkedList<>();
	private final StacktraceThread shutdownHook;

	private DynamicObject base;
	private OzProc main;

	public OzContext(OzLanguage language, Env env) {
		if (OzContext.instance != null) {
			throw new Error("More than 1 context");
		} else {
			OzContext.instance = this;
		}

		this.env = env;
		this.language = language;
		this.home = language.getHome();

		this.translatorDriver = new TranslatorDriver(language);

		BuiltinsManager.defineBuiltins(language);

		propertyRegistry = PropertyRegistry.INSTANCE;
		propertyRegistry.initialize(home);

		mainImage = new File(home, "Main.image");

		if (Options.STACKTRACE_ON_INTERRUPT) {
			this.shutdownHook = new StacktraceThread();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} else {
			this.shutdownHook = null;
		}
	}

	public String getHome() {
		return home;
	}

	public void initialize() {
		loadMain();
	}

	public void finalizeContext() {
		waitThreads();

		if (Options.STACKTRACE_ON_INTERRUPT) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
	}

	private void waitThreads() {
		while (OzThread.getNumberOfThreadsRunnable() > 1) {
			OzThread.getCurrent().yield(null);
		}
	}

	private void loadMain() {
		Metrics.tick("start loading Main");
		String initFunctorPath = home + "/lib/main/init/Init.oz";

		if (Options.SERIALIZER && mainImage.exists()) {
			try (OzSerializer serializer = new OzSerializer(env, language, initFunctorPath)) {
				main = serializer.deserialize(mainImage.getPath(), OzProc.class);
			} catch (Throwable t) {
				System.err.println("Got " + t.getClass().getSimpleName() + " while deserializing, removing Main.image");
				mainImage.delete();
				throw t;
			}
			Metrics.tick("deserialized Main");
		} else {
			translatorDriver.setEagerLoad(true);
			try {
				Source initSource = Loader.createSource(env, initFunctorPath);
				RootCallTarget initCallTarget = translatorDriver.parseFunctor(initSource);
				Object initFunctor = execute(initCallTarget);
				Object applied = applyInitFunctor(initFunctor);
				main = (OzProc) ((DynamicObject) applied).get("main");

				if (Options.SERIALIZER) {
					try (OzSerializer serializer = new OzSerializer(env, language, initFunctorPath)) {
						serializer.serialize(main, mainImage.getPath());
					}
				}
			} finally {
				translatorDriver.setEagerLoad(false);
			}
		}
	}

	@TruffleBoundary
	public void run(Source source) {
		propertyRegistry.setApplicationURL(source.getPath());
		propertyRegistry.setApplicationArgs(env.getApplicationArguments());

		main.rootCall("main");
	}

	// Base

	public boolean isLoadingBase() {
		return base == null;
	}

	private DynamicObject loadBase() {
		if (base == null) {
			Metrics.tick("start loading Base");
			Source source = Loader.createSource(env, home + "/lib/main/base/Base.oz");
			RootCallTarget baseFunctorTarget = translatorDriver.parseBase(source);
			Metrics.tick("translated Base");
			Object baseFunctor = execute(baseFunctorTarget);

			Object imports = BuiltinsManager.getBootModulesRecord();
			RootCallTarget applyBase = ApplyFunctor.apply(language, baseFunctor, imports, "Base.apply");
			Object result = execute(applyBase);
			assert result instanceof DynamicObject;

			base = (DynamicObject) result;
		}
		return base;
	}

	public void registerBase(DynamicObject base) {
		assert this.base == null || this.base == base;
		this.base = base;
	}

	public DynamicObject getBase() {
		return loadBase();
	}

	// Misc

	private Object applyInitFunctor(Object initFunctor) {
		Object imports = OzRecord.buildRecord(
				Arity.build("import", "Boot"),
				BuiltinsManager.getBootModule("Boot_Boot"));
		return execute(ApplyFunctor.apply(language, initFunctor, imports, "Init.apply"));
	}

	public Object execute(RootCallTarget callTarget) {
		Object[] arguments = OzArguments.pack(null, ArrayUtils.EMPTY);
		Object value = callTarget.call(arguments);
		Metrics.tick("executed " + callTarget.getRootNode().getName());
		return value;
	}

	// Shutdown

	public void registerChildProcess(Process process) {
		childProcesses.add(process);
	}

	@TruffleBoundary
	public void shutdown(int exitCode) {
		for (Process process : childProcesses) {
			process.destroyForcibly();
		}
		if (Options.PRINT_NVARS) {
			System.out.println("nvars --- " + Variable.variableCount);
		}
		System.exit(exitCode);
	}

	// Getters

	public Env getEnv() {
		return env;
	}

	public TranslatorDriver getTranslatorDriver() {
		return translatorDriver;
	}

}
