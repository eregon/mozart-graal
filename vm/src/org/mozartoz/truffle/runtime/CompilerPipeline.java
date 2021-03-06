package org.mozartoz.truffle.runtime;

import org.graalvm.options.OptionValues;
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

public class CompilerPipeline {

	public static Statement compile(Program program, OptionValues options, String fileName) {
		Statement ast = applyTransformations(program, options);
		BootCompiler.checkCompileErrors(program, fileName);
		return ast;
	}

	private static Statement applyTransformations(Program program, OptionValues options) {
		Namer.apply(program);
		DesugarFunctor.apply(program);
		DesugarClass.apply(program);
		Desugar.apply(program);

		ConstantFolding.apply(program);
		Unnester.apply(program);

		if (options.get(Options.DIRECT_VARS)) {
			new OnStackMarking().apply(program);
		}
		TailCallMarking.apply(program);
		if (options.get(Options.FREE_SLOTS)) {
			assert options.get(Options.FRAME_FILTERING) : "";
			VariableDeduplication.apply(program);
			VariableClearing.apply(program);
		}

		return program.rawCode();
	}

}
