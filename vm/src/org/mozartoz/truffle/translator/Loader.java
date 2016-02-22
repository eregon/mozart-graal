package org.mozartoz.truffle.translator;

import java.io.File;
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
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;

import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.HashSet;
import scala.collection.immutable.List;
import scala.collection.immutable.Set;
import scala.util.parsing.input.Position;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class Loader {

	public static final String PROJECT_ROOT = getProjectRoot();
	static final String MODULE_DEFS_DIR = PROJECT_ROOT + "/builtins";
	static final String BASE_FILE_NAME = "/home/eregon/code/mozart2/lib/main/base/Base.oz";
	static final String BASE_DECLS_FILE_NAME = PROJECT_ROOT + "/baseenv.txt";

	// public static final PolyglotEngine ENGINE = PolyglotEngine.newBuilder().build();

	private static final Loader INSTANCE = new Loader();

	public static Loader getInstance() {
		return INSTANCE;
	}

	private DynamicObject base = null;

	private Loader() {
		BuiltinsManager.defineBuiltins();
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

	public DynamicObject loadBase() {
		if (base == null) {
			OzRootNode baseRootNode = parseBase();
			Object result = execute(baseRootNode);
			assert result instanceof DynamicObject;
			base = (DynamicObject) result;
		}
		return base;
	}

	public OzRootNode parse(Source source) {
		DynamicObject base = loadBase();

		String fileName = new File(source.getPath()).getName();
		Program program = Main.buildNormalProgram(fileName, moduleDefs(), BASE_DECLS_FILE_NAME, defines());
		Statement ast = compile(program, fileName);

		Translator translator = new Translator();
		FrameSlot baseSlot = translator.addRootSymbol(program.baseEnvSymbol());
		return translator.translateAST(fileName, ast, node -> {
			return SequenceNode.sequence(
					new InitializeTmpNode(baseSlot, new LiteralNode(base)),
					node);
		});
	}

	public void run(Source source) {
		execute(parse(source));
	}

	private static Object execute(OzRootNode rootNode) {
		return Truffle.getRuntime().createCallTarget(rootNode).call();
	}

	private Statement compile(Program program, String fileName) {
		Statement ast = applyTransformations(program);
		if (program.hasErrors()) {
			System.err.println("There were errors while compiling " + fileName);
			for (Tuple2<String, Position> error : JavaConversions.asJavaCollection(program.errors())) {
				System.err.println("Error at " + error._2);
				System.err.println(error._1);
				System.err.println(error._2.longString());
				System.exit(2);
			}
		}
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
		return javaListToScalaList(Collections.singletonList(MODULE_DEFS_DIR)).toList();
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
