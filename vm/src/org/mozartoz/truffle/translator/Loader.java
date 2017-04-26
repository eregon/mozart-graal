package org.mozartoz.truffle.translator;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.mozartoz.bootcompiler.BootCompiler;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.symtab.Program;
import org.mozartoz.bootcompiler.transform.ConstantFolding;
import org.mozartoz.bootcompiler.transform.Desugar;
import org.mozartoz.bootcompiler.transform.DesugarClass;
import org.mozartoz.bootcompiler.transform.DesugarFunctor;
import org.mozartoz.bootcompiler.transform.Namer;
import org.mozartoz.bootcompiler.transform.OnStackMarking;
import org.mozartoz.bootcompiler.transform.TailCallMarking;
import org.mozartoz.bootcompiler.transform.Unnester;
import org.mozartoz.bootcompiler.transform.VariableClearing;
import org.mozartoz.bootcompiler.transform.VariableDeduplication;
import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.ArrayUtils;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzThread;
import org.mozartoz.truffle.runtime.PropertyRegistry;
import org.mozartoz.truffle.runtime.StacktraceThread;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.tools.Profiler;

public class Loader {

	public static final String PROJECT_ROOT = getProjectRoot();
	public static final String LIB_DIR = PROJECT_ROOT + "/lib";
	public static final String MAIN_LIB_DIR = LIB_DIR + "/main";

	static final String TOOLS_DIR = LIB_DIR + "/tools";
	static final String BASE_FILE_NAME = MAIN_LIB_DIR + "/base/Base.oz";
	static final String INIT_FUNCTOR = MAIN_LIB_DIR + "/init/Init.oz";

	static final String MAIN_IMAGE = PROJECT_ROOT + "/Main.image";

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

	public static final Source GET_LANGUAGE_SOURCE = buildInternalSource("language");

	public static final Source MAIN_SOURCE = buildInternalSource("main");
	public static final SourceSection MAIN_SOURCE_SECTION = MAIN_SOURCE.createUnavailableSection();

	private static long last = System.currentTimeMillis();

	private static void tick(String desc) {
		if (Options.MEASURE_STARTUP) {
			long now = System.currentTimeMillis();
			long duration = now - last;
			if (duration > 5) {
				System.out.println(String.format("%4d", duration) + " " + desc);
			}
			last = now;
		}
	}

	private static final Loader INSTANCE = new Loader();

	public static Loader getInstance() {
		return INSTANCE;
	}

	private DynamicObject base = null;
	private final PropertyRegistry propertyRegistry;
	private final PolyglotEngine engine;
	private final OzLanguage language;
	private StacktraceThread shutdownHook;

	private Loader() {
		engine = PolyglotEngine.newBuilder().build();
		language = engine.eval(GET_LANGUAGE_SOURCE).as(OzLanguage.class);
		BuiltinsManager.defineBuiltins(language);
		propertyRegistry = PropertyRegistry.INSTANCE;
		propertyRegistry.initialize();

		if (Options.STACKTRACE_ON_INTERRUPT) {
			shutdownHook = new StacktraceThread();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}

		if (Options.PROFILER) {
			setupProfiler();
		}
	}

	private void setupProfiler() {
		Profiler profiler = Profiler.find(engine);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> profiler.printHistograms(System.err)));
		profiler.setCollecting(true);
		profiler.setTiming(true);
	}

	public PolyglotEngine getEngine() {
		return engine;
	}

	public boolean isLoadingBase() {
		return base == null;
	}

	public DynamicObject loadBase() {
		if (base == null) {
			tick("start loading Base");
			RootCallTarget baseFunctorTarget = parseBase();
			tick("translated Base");
			Object baseFunctor = execute(baseFunctorTarget);

			Object imports = BuiltinsManager.getBootModulesRecord();
			RootCallTarget applyBase = ApplyFunctor.apply(language, baseFunctor, imports, "Base.apply");
			Object result = execute(applyBase);
			assert result instanceof DynamicObject;

			base = (DynamicObject) result;
		}
		return base;
	}

	private RootCallTarget parseBase() {
		tick("enter parseBase");
		Program program = BootCompiler.buildBaseEnvProgram(
				createSource(BASE_FILE_NAME),
				BuiltinsRegistry.getBuiltins());
		tick("parse Base");
		Statement ast = compile(program, "the base environment");

		Translator translator = new Translator(language, null);
		FrameSlot topLevelResultSlot = translator.addRootSymbol(program.topLevelResultSymbol());
		return translator.translateAST("<Base>", ast, node -> {
			return SequenceNode.sequence(
					new InitializeVarNode(topLevelResultSlot),
					node,
					DerefNode.create(new ReadLocalVariableNode(topLevelResultSlot)));
		});
	}

	public RootCallTarget parseMain(Source source) {
		DynamicObject base = loadBase();

		String fileName = new File(source.getPath()).getName();
		Program program = BootCompiler.buildMainProgram(source, BuiltinsRegistry.getBuiltins());
		Statement ast = compile(program, fileName);

		Translator translator = new Translator(language, base);
		FrameSlot baseSlot = translator.addRootSymbol(program.baseEnvSymbol());
		return translator.translateAST(fileName, ast, node -> {
			return SequenceNode.sequence(
					new InitializeTmpNode(baseSlot, new LiteralNode(base)),
					node);
		});
	}

	private boolean eagerLoad = false;

	public RootCallTarget parseFunctor(Source source) {
		DynamicObject base = loadBase();

		String fileName = new File(source.getPath()).getName();
		System.out.println("Loading " + fileName);
		tick("start parse");
		Program program = BootCompiler.buildProgram(source, false, eagerLoad, BuiltinsRegistry.getBuiltins());
		tick("parse functor " + fileName);
		Statement ast = compile(program, fileName);
		tick("compiled functor " + fileName);

		Translator translator = new Translator(language, base);
		FrameSlot baseSlot = translator.addRootSymbol(program.baseEnvSymbol());
		FrameSlot topLevelResultSlot = translator.addRootSymbol(program.topLevelResultSymbol());
		RootCallTarget callTarget = translator.translateAST(fileName, ast, node -> {
			return SequenceNode.sequence(
					new InitializeTmpNode(baseSlot, new LiteralNode(base)),
					new InitializeVarNode(topLevelResultSlot),
					node,
					DerefNode.create(new ReadLocalVariableNode(topLevelResultSlot)));
		});
		tick("translated functor " + fileName);
		return callTarget;
	}

	public void run(Source source) {
		execute(parseMain(source));
	}

	public void runFunctor(Source source, String... args) {
		tick("start loading Main");
		final OzProc main;
		if (Options.SERIALIZER && new File(MAIN_IMAGE).exists()) {
			try (OzSerializer serializer = new OzSerializer(language)) {
				main = serializer.deserialize(MAIN_IMAGE, OzProc.class);
			} catch (Throwable t) {
				System.err.println("Got " + t.getClass().getSimpleName() + " while deserializing, removing Main.image");
				new File(MAIN_IMAGE).delete();
				throw t;
			}
			tick("deserialized Main");
			base = getBaseFromMain(main);
		} else {
			eagerLoad = true;
			try {
				// The first execution of code needs to go through PolyglotEngine
				// to initialize it for instrumentation purposes.
				Object initFunctor = engine.eval(createSource(INIT_FUNCTOR)).as(DynamicObject.class);
				Object applied = applyInitFunctor(initFunctor);
				main = (OzProc) ((DynamicObject) applied).get("main");
				if (Options.SERIALIZER) {
					try (OzSerializer serializer = new OzSerializer(language)) {
						serializer.serialize(main, MAIN_IMAGE);
					}
				}
			} finally {
				eagerLoad = false;
			}
		}

		propertyRegistry.setApplicationURL(source.getPath());
		propertyRegistry.setApplicationArgs(args);

		main.rootCall("main");

		waitThreads();
		shutdown();

		if (Options.PRINT_NVARS) {
			System.out.println("nvars --- " + Variable.variableCount);
		}
	}

	private void waitThreads() {
		while (OzThread.getNumberOfThreadsRunnable() > 1) {
			OzThread.getCurrent().yield(null);
		}
	}

	private void shutdown() {
		if (Options.STACKTRACE_ON_INTERRUPT) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
	}

	private Object applyInitFunctor(Object initFunctor) {
		Object imports = OzRecord.buildRecord(
				Arity.build("import", "Boot"),
				BuiltinsManager.getBootModule("Boot_Boot"));
		return execute(ApplyFunctor.apply(language, initFunctor, imports, "Init.apply"));
	}

	private DynamicObject getBaseFromMain(OzProc main) {
		MaterializedFrame topFrame = OzArguments.getParentFrame(main.declarationFrame);
		FrameSlot baseSlot = topFrame.getFrameDescriptor().getSlots().get(0);
		assert baseSlot.getIdentifier().toString().contains("<Base>");
		return (DynamicObject) topFrame.getValue(baseSlot);
	}

	// Shutdown

	private List<Process> childProcesses = new LinkedList<>();

	public void registerChildProcess(Process process) {
		childProcesses.add(process);
	}

	@TruffleBoundary
	public void shutdown(int exitCode) {
		for (Process process : childProcesses) {
			process.destroyForcibly();
		}
		System.exit(exitCode);
	}

	// Helpers

	public static Source createSource(String path) {
		File file = new File(path);
		try {
			return Source.newBuilder(file).name(file.getName()).mimeType(OzLanguage.MIME_TYPE).build();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public static Source buildInternalSource(String name) {
		return Source.newBuilder("").name(name).mimeType(OzLanguage.MIME_TYPE).internal().build();
	}

	public Object execute(RootCallTarget callTarget) {
		Object[] arguments = OzArguments.pack(null, ArrayUtils.EMPTY);
		Object value = callTarget.call(arguments);
		tick("executed " + callTarget.getRootNode().getName());
		return value;
	}

	private Statement compile(Program program, String fileName) {
		Statement ast = applyTransformations(program);
		BootCompiler.checkCompileErrors(program, fileName);
		return ast;
	}

	private Statement applyTransformations(Program program) {
		Namer.apply(program);
		DesugarFunctor.apply(program);
		DesugarClass.apply(program);
		Desugar.apply(program);

		ConstantFolding.apply(program);
		Unnester.apply(program);

		if (Options.DIRECT_VARS) {
			new OnStackMarking().apply(program);
		}
		TailCallMarking.apply(program);
		if (Options.FREE_SLOTS) {
			assert Options.FRAME_FILTERING : "";
			VariableDeduplication.apply(program);
			VariableClearing.apply(program);
		}

		return program.rawCode();
	}

	private static String getProjectRoot() {
		String thisFile = Loader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = new File(thisFile).getParentFile().getParent();
		return path;
	}

}
