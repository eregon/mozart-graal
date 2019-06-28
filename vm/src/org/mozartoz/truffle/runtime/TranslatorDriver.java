package org.mozartoz.truffle.runtime;

import java.io.File;

import org.graalvm.options.OptionValues;
import org.mozartoz.bootcompiler.BootCompiler;
import org.mozartoz.bootcompiler.BootCompilerOptions;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.symtab.Program;
import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.translator.BuiltinsRegistry;
import org.mozartoz.truffle.translator.Translator;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class TranslatorDriver {

	private final OzLanguage language;
	private final BootCompilerOptions options;

	public TranslatorDriver(OzLanguage language) {
		this.language = language;
		BootCompiler.registerParserToVM(ParserToVMImpl.INSTANCE);
		this.options = new BootCompilerOptions(Options.SELF_TAIL_CALLS, Options.FRAME_FILTERING);
	}

	public RootCallTarget parseBase(Source source) {
		Metrics.tick("enter parseBase");
		Program program = BootCompiler.buildBaseEnvProgram(new SourceWrapper(source), BuiltinsRegistry.getBuiltins(), options);
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

	public RootCallTarget parseFunctor(Source source, boolean eagerLoad) {
		DynamicObject base = OzLanguage.getContext().getBase();

		String fileName = new File(source.getPath()).getName();
		System.out.println("Loading " + fileName);
		Metrics.tick("start parse");
		Program program = BootCompiler.buildProgram(new SourceWrapper(source), false, eagerLoad, BuiltinsRegistry.getBuiltins(), options);
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

}
