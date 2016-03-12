package org.mozartoz.truffle.translator;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.mozartoz.bootcompiler.Main;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.symtab.Program;
import org.mozartoz.bootcompiler.transform.ConstantFolding;
import org.mozartoz.bootcompiler.transform.Desugar;
import org.mozartoz.bootcompiler.transform.DesugarClass;
import org.mozartoz.bootcompiler.transform.DesugarFunctor;
import org.mozartoz.bootcompiler.transform.Namer;
import org.mozartoz.bootcompiler.transform.PatternMatcher;
import org.mozartoz.bootcompiler.transform.Unnester;
import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.PropertyBuiltins;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzLanguage;

import scala.collection.JavaConversions;
import scala.collection.immutable.HashSet;
import scala.collection.immutable.List;
import scala.collection.immutable.Set;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class Loader {

	public static final String PROJECT_ROOT = getProjectRoot();
	static final String MODULE_DEFS_DIR = PROJECT_ROOT + "/builtins";
	public static final String MOZART2_DIR = new File(PROJECT_ROOT).getParent() + "/mozart2";
	public static final String MAIN_LIB_DIR = MOZART2_DIR + "/lib/main";
	static final String BASE_FILE_NAME = MAIN_LIB_DIR + "/base/Base.oz";
	static final String BASE_DECLS_FILE_NAME = PROJECT_ROOT + "/baseenv.txt";
	static final String INIT_FUNCTOR = MAIN_LIB_DIR + "/init/Init.oz";

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
	};

	// public static final PolyglotEngine ENGINE = PolyglotEngine.newBuilder().build();

	private static final Loader INSTANCE = new Loader();

	public static Loader getInstance() {
		return INSTANCE;
	}

	private DynamicObject base = null;

	private Loader() {
		BuiltinsManager.defineBuiltins();
	}

	public DynamicObject loadBase() {
		if (base == null) {
			OzRootNode baseRootNode = parseBase();
			Object result = execute(baseRootNode);
			assert result instanceof DynamicObject;
			base = (DynamicObject) result;
		}
		return base;
	}

	private OzRootNode parseBase() {
		Program program = Main.buildBaseEnvProgram(BASE_FILE_NAME, moduleDefs(), defines());
		Statement ast = compile(program, "the base environment");

		Translator translator = new Translator();
		FrameSlot topLevelResultSlot = translator.addRootSymbol(program.topLevelResultSymbol());
		return translator.translateAST("<Base>", ast, node -> {
			return SequenceNode.sequence(
					new InitializeVarNode(topLevelResultSlot),
					node,
					DerefNodeGen.create(new ReadLocalVariableNode(topLevelResultSlot)));
		});
	}

	public OzRootNode parseMain(Source source) {
		DynamicObject base = loadBase();

		String fileName = new File(source.getPath()).getName();
		Program program = Main.buildMainProgram(source.getPath(), moduleDefs(), BASE_DECLS_FILE_NAME, defines());
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
		Program program = Main.buildModuleProgram(source.getPath(), moduleDefs(), BASE_DECLS_FILE_NAME, defines());
		Statement ast = compile(program, fileName);

		Translator translator = new Translator();
		FrameSlot baseSlot = translator.addRootSymbol(program.baseEnvSymbol());
		FrameSlot topLevelResultSlot = translator.addRootSymbol(program.topLevelResultSymbol());
		return translator.translateAST(fileName, ast, node -> {
			return SequenceNode.sequence(
					new InitializeTmpNode(baseSlot, new LiteralNode(base)),
					new InitializeVarNode(topLevelResultSlot),
					node,
					DerefNodeGen.create(new ReadLocalVariableNode(topLevelResultSlot)));
		});
	}

	public void run(Source source) {
		execute(parseMain(source));
	}

	public void runFunctor(Source source, String... args) {
		Object initFunctor = execute(parseFunctor(createSource(INIT_FUNCTOR)));

		PropertyBuiltins.setApplicationURL(source.getPath());
		PropertyBuiltins.setApplicationArgs(args);
		execute(InitFunctor.apply(initFunctor));
	}

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
		Object[] arguments = OzArguments.pack(null, new Object[0]);
		return Truffle.getRuntime().createCallTarget(rootNode).call(arguments);
	}

	private Statement compile(Program program, String fileName) {
		Statement ast = applyTransformations(program);
		Main.checkCompileErrors(program, fileName);
		return ast;
	}

	private Statement applyTransformations(Program program) {
		Namer.apply(program);
		DesugarFunctor.apply(program);
		DesugarClass.apply(program);
		Desugar.apply(program);
		PatternMatcher.apply(program);

		ConstantFolding.apply(program);
		Unnester.apply(program);

		return program.rawCode();
	}

	private List<String> moduleDefs() {
		return javaListToScalaList(Collections.singletonList(MODULE_DEFS_DIR));
	}

	private Set<String> defines() {
		return new HashSet<String>();
	}

	static <T> List<T> javaListToScalaList(java.util.List<T> javaList) {
		return JavaConversions.asScalaBuffer(javaList).toList();
	}

	private static String getProjectRoot() {
		String thisFile = Loader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = new File(thisFile).getParent();
		return path;
	}

}
