package org.mozartoz.truffle.runtime;

import java.io.File;
import java.io.IOException;

import org.mozartoz.bootcompiler.BootCompiler;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.symtab.Program;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.translator.BuiltinsRegistry;
import org.mozartoz.truffle.translator.Loader;
import org.mozartoz.truffle.translator.Translator;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class TranslatorDriver {

	private final OzLanguage language;

	private boolean eagerLoad = false;

	public TranslatorDriver(OzLanguage language) {
		this.language = language;
	}

	public RootCallTarget parseBase() {
		Metrics.tick("enter parseBase");
		Program program = BootCompiler.buildBaseEnvProgram(
				createSource(Loader.BASE_FILE_NAME),
				BuiltinsRegistry.getBuiltins());
		Metrics.tick("parse Base");
		Statement ast = CompilerPipeline.compile(program, "the base environment");

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
		DynamicObject base = OzContext.getInstance().getBase();

		String fileName = new File(source.getPath()).getName();
		Program program = BootCompiler.buildMainProgram(source, BuiltinsRegistry.getBuiltins());
		Statement ast = CompilerPipeline.compile(program, fileName);

		Translator translator = new Translator(language, base);
		FrameSlot baseSlot = translator.addRootSymbol(program.baseEnvSymbol());
		return translator.translateAST(fileName, ast, node -> {
			return SequenceNode.sequence(
					new InitializeTmpNode(baseSlot, new LiteralNode(base)),
					node);
		});
	}

	public RootCallTarget parseFunctor(Source source) {
		DynamicObject base = OzContext.getInstance().getBase();

		String fileName = new File(source.getPath()).getName();
		System.out.println("Loading " + fileName);
		Metrics.tick("start parse");
		Program program = BootCompiler.buildProgram(source, false, eagerLoad, BuiltinsRegistry.getBuiltins());
		Metrics.tick("parse functor " + fileName);
		Statement ast = CompilerPipeline.compile(program, fileName);
		Metrics.tick("compiled functor " + fileName);

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
		Metrics.tick("translated functor " + fileName);
		return callTarget;
	}

	public void setEagerLoad(boolean value) {
		this.eagerLoad = value;
	}

	public static Source createSource(String path) {
		File file = new File(path);
		try {
			return Source.newBuilder(file).name(file.getName()).mimeType(OzLanguage.MIME_TYPE).build();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
