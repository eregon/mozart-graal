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
import org.mozartoz.bootcompiler.transform.PatternMatcher;
import org.mozartoz.bootcompiler.transform.Simplify;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.PropertyRegistry;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class Loader {

	public static final String PROJECT_ROOT = getProjectRoot();
	public static final String MOZART2_DIR = new File(PROJECT_ROOT).getParent() + "/mozart2";
	public static final String LOCAL_LIB_DIR = PROJECT_ROOT + "/lib";

	static final String MAIN_LIB_DIR = MOZART2_DIR + "/lib/main";
	static final String TOOLS_DIR = MOZART2_DIR + "/lib/tools";
	static final String BASE_FILE_NAME = MAIN_LIB_DIR + "/base/Base.oz";
	static final String INIT_FUNCTOR = MAIN_LIB_DIR + "/init/Init.oz";

	static final String BASE_IMAGE = PROJECT_ROOT + "/Base.image";

	public static final String OZWISH = MOZART2_DIR + "/wish/ozwish";

	public static final String[] SYSTEM_FUNCTORS = new String[] {
			MAIN_LIB_DIR + "/sys/Property.oz",
			MAIN_LIB_DIR + "/sys/Space.oz",
			MAIN_LIB_DIR + "/sys/Pickle.oz",
			MAIN_LIB_DIR + "/support/CompilerSupport.oz",
			MAIN_LIB_DIR + "/support/ErrorListener.oz",
			MAIN_LIB_DIR + "/support/Listener.oz",
			MAIN_LIB_DIR + "/support/Narrator.oz",
			MAIN_LIB_DIR + "/support/ObjectSupport.oz",
			MAIN_LIB_DIR + "/support/Type.oz",
			MAIN_LIB_DIR + "/sp/Error.oz",
			MAIN_LIB_DIR + "/sp/ErrorFormatters.oz",
			MAIN_LIB_DIR + "/op/Open.oz",
			MAIN_LIB_DIR + "/cp/Combinator.oz",
			MAIN_LIB_DIR + "/cp/RecordC.oz",
			MAIN_LIB_DIR + "/ap/Application.oz",
			MOZART2_DIR + "/vm/boostenv/lib/OS.oz",
			MAIN_LIB_DIR + "/wp/Tk.oz",
			MAIN_LIB_DIR + "/wp/TkBoot.oz",
			MAIN_LIB_DIR + "/wp/TkTools.oz",
			TOOLS_DIR + "/panel/Panel.oz",
			TOOLS_DIR + "/browser/Browser.oz",
	};

	// public static final PolyglotEngine ENGINE = PolyglotEngine.newBuilder().build();

	private static final boolean MEASURE_STARTUP = System.getProperty("oz.measure.startup") != null;

	private static long last = System.currentTimeMillis();

	private static void tick(String desc) {
		if (MEASURE_STARTUP) {
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

	private Loader() {
		BuiltinsManager.defineBuiltins();
		propertyRegistry = PropertyRegistry.INSTANCE;
		propertyRegistry.initialize();
	}

	public DynamicObject loadBase() {
		if (base == null) {
			tick("start loading Base");
			if (new File(BASE_IMAGE).exists()) {
				base = OzSerializer.deserialize(BASE_IMAGE, DynamicObject.class);
				tick("deserialized Base");
				return base;
			}

			OzRootNode baseRootNode = parseBase();
			tick("translated Base");
			Object baseFunctor = execute(baseRootNode);

			OzRootNode applyBase = BaseFunctor.apply(baseFunctor);
			Object result = execute(applyBase);
			assert result instanceof DynamicObject;

			base = (DynamicObject) result;

			OzSerializer.serialize(base, BASE_IMAGE);
			tick("serialized Base");

			DynamicObject reloaded = OzSerializer.deserialize(BASE_IMAGE, DynamicObject.class);
			tick("deserialized Base");
			base = reloaded;
		}
		return base;
	}

	private OzRootNode parseBase() {
		tick("enter parseBase");
		Program program = BootCompiler.buildBaseEnvProgram(BASE_FILE_NAME, BuiltinsRegistry.getBuiltins(), BaseDeclarations.getDeclarations());
		tick("parse Base");
		Statement ast = compile(program, "the base environment");

		Translator translator = new Translator();
		FrameSlot topLevelResultSlot = translator.addRootSymbol(program.topLevelResultSymbol());
		return translator.translateAST("<Base>", ast, node -> {
			return SequenceNode.sequence(
					new InitializeVarNode(topLevelResultSlot),
					node,
					DerefNode.create(new ReadLocalVariableNode(topLevelResultSlot)));
		});
	}

	public OzRootNode parseMain(Source source) {
		DynamicObject base = loadBase();

		String fileName = new File(source.getPath()).getName();
		Program program = BootCompiler.buildMainProgram(source.getPath(), BuiltinsRegistry.getBuiltins(), BaseDeclarations.getDeclarations());
		Statement ast = compile(program, fileName);

		Translator translator = new Translator();
		FrameSlot baseSlot = translator.addRootSymbol(program.baseEnvSymbol());
		return translator.translateAST(fileName, ast, node -> {
			return SequenceNode.sequence(
					new InitializeTmpNode(baseSlot, new LiteralNode(base)),
					node);
		});
	}

	public OzRootNode parseFunctor(Source source) {
		DynamicObject base = loadBase();

		String fileName = new File(source.getPath()).getName();
		System.out.println("Loading " + fileName);
		tick("start parse");
		Program program = BootCompiler.buildModuleProgram(source.getPath(), BuiltinsRegistry.getBuiltins(), BaseDeclarations.getDeclarations());
		tick("parse functor " + fileName);
		Statement ast = compile(program, fileName);
		tick("compiled functor " + fileName);

		Translator translator = new Translator();
		FrameSlot baseSlot = translator.addRootSymbol(program.baseEnvSymbol());
		FrameSlot topLevelResultSlot = translator.addRootSymbol(program.topLevelResultSymbol());
		OzRootNode rootNode = translator.translateAST(fileName, ast, node -> {
			return SequenceNode.sequence(
					new InitializeTmpNode(baseSlot, new LiteralNode(base)),
					new InitializeVarNode(topLevelResultSlot),
					node,
					DerefNode.create(new ReadLocalVariableNode(topLevelResultSlot)));
		});
		tick("translated functor " + fileName);
		return rootNode;
	}

	public void run(Source source) {
		execute(parseMain(source));
	}

	public void runFunctor(Source source, String... args) {
		Object initFunctor = execute(parseFunctor(createSource(INIT_FUNCTOR)));

		propertyRegistry.setApplicationURL(source.getPath());
		propertyRegistry.setApplicationArgs(args);
		execute(InitFunctor.apply(initFunctor));
	}

	// Shutdown

	private List<Process> childProcesses = new LinkedList<>();

	public void registerChildProcess(Process process) {
		childProcesses.add(process);
	}

	public void shutdown(int exitCode) {
		for (Process process : childProcesses) {
			process.destroyForcibly();
		}
		System.exit(exitCode);
	}

	// Helpers

	public static Source createSource(String path) {
		Source source;
		try {
			source = Source.fromFileName(path);
		} catch (IOException e) {
			throw new Error(e);
		}
		return source.withMimeType(OzLanguage.MIME_TYPE);
	}

	public Object execute(OzRootNode rootNode) {
		RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
		Object[] arguments = OzArguments.pack(null, new Object[0]);
		Object value = callTarget.call(arguments);
		tick("executed " + rootNode.getSourceSection().getIdentifier());
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
		PatternMatcher.apply(program);

		ConstantFolding.apply(program);
		Simplify.apply(program);

		return program.rawCode();
	}

	private static String getProjectRoot() {
		String thisFile = Loader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = new File(thisFile).getParent();
		return path;
	}

}
