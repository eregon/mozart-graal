package org.mozartoz.truffle;

import java.io.File;
import java.io.IOException;

import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.translator.Translator;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;

public class Main {

	public static final PolyglotEngine ENGINE = PolyglotEngine.newBuilder().build();

	public static final String PROJECT_ROOT = getProjectRoot();

	public static void main(String[] args) throws IOException {
		eval("{Show 3 + 2}");
		eval("local A=3 B=2 in {Show A + B} end");
		eval("local fun {Inc Arg} Arg + 1 end in {Show {Inc 1}} end");
		eval("local Fact in\nfun {Fact N} if N == 0 then 1 else N * {Fact N-1} end end\n{Show {Fact 30}}\nend");
		eval("local L in L = [1 2 3] {Show L} {Show L.1} {Show L.2} end");
		eval("local A in A=1+2 {Show A} end");
		eval("local A B in A=B B=42 {Show B} {Show A} end");

		eval("local Add in fun {Add A B} A + B end {Show {Add 2 4}} end");
		eval("local H=5 V T in T=V V=[6 7 8] {Show H|T} end");
		eval("local H=5 V T in V=T {Show H|T} {proc {$} V=[6 7 8] end} {Show H|T} end");
		eval("local fun {Binder A} A=43 unit end X R in R={Binder X} {Show X} end");

		eval("local Tree=tree(left:1 right:2) in {Show Tree} {Show Tree.left} end");

		eval("local F=functor\n import\n Property(get)\n export\n Return\n define\n Return=42\n end in {Show F} end");

		eval("local fun {MakeList N} if N>0 then _|{MakeList N-1} else nil end end in {Show {MakeList 3}} end");

		eval("local fun {Append Xs Ys} case Xs of nil then Ys [] X|Xr then X|{Append Xr Ys} end end in {Show {Append [1 2] [3 4]}} end");

		eval("local fun {Member X Ys} case Ys of nil then false [] Y|Yr then X==Y orelse {Member X Yr} end end in {Show {Member nil [1 2 3]}} end");

		Source source = Source.fromFileName(PROJECT_ROOT + "/test.oz");
		eval(source.getCode());
	}

	private static void eval(String code) {
		// ENGINE.eval(Source.fromText(code, "test.oz").withMimeType(OzLanguage.MIME_TYPE));
		parseAndExecute(code);
	}

	public static void parseAndExecute(String code) {
		execute(new Translator().parseAndTranslate(code));
	}

	private static Object execute(OzRootNode rootNode) {
		return Truffle.getRuntime().createCallTarget(rootNode).call();
	}

	private static String getProjectRoot() {
		String thisFile = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = new File(thisFile).getParent();
		return path;
	}

}
