package org.mozartoz.truffle;

import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.translator.Translator;

import com.oracle.truffle.api.Truffle;

public class Main {
	public static void main(String[] args) {
		parseAndExecute("{Show 3 + 2}");
		parseAndExecute("local A=3 B=2 in {Show A + B} end");
		parseAndExecute("local fun {Inc Arg} Arg + 1 end in {Show {Inc 1}} end");
		parseAndExecute("local Fact in\nfun {Fact N} if N == 0 then 1 else N * {Fact N-1} end end\n{Show {Fact 30}}\nend");
		parseAndExecute("local L in L = [1 2 3] {Show L} {Show L.1} {Show L.2} end");
		parseAndExecute("local A in A=1+2 {Show A} end");
		parseAndExecute("local A B in A=B B=42 {Show B} {Show A} end");

		parseAndExecute("local Add in fun {Add A B} A + B end {Show {Add 2 4}} end");
		parseAndExecute("local H=5 V T in T=V V=[6 7 8] {Show H|T} end");
		parseAndExecute("local H=5 V T in V=T {Show H|T} V=[6 7 8] {Show H|T} end");
		parseAndExecute("local fun {Binder A} A=43 unit end X R in R={Binder X} {Show X} end");
	}

	private static void parseAndExecute(String code) {
		execute(new Translator().parseAndTranslate(code));
	}

	private static Object execute(OzRootNode rootNode) {
		return Truffle.getRuntime().createCallTarget(rootNode).call();
	}

}
