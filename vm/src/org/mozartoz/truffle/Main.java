package org.mozartoz.truffle;

import org.mozartoz.truffle.nodes.IfNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.SequenceNode;
import org.mozartoz.truffle.nodes.builtins.AddNodeGen;
import org.mozartoz.truffle.nodes.builtins.EqualNodeGen;
import org.mozartoz.truffle.nodes.builtins.MulNodeGen;
import org.mozartoz.truffle.nodes.builtins.ShowNodeGen;
import org.mozartoz.truffle.nodes.builtins.SubNodeGen;
import org.mozartoz.truffle.nodes.literal.FunctionDeclarationNode;
import org.mozartoz.truffle.nodes.literal.ListLiteralNode;
import org.mozartoz.truffle.nodes.literal.LongLiteralNode;
import org.mozartoz.truffle.nodes.local.BindVariableValueNode;
import org.mozartoz.truffle.nodes.local.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.translator.Translator;

import call.CallFunctionNodeGen;
import call.ReadArgumentNode;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;

public class Main {
	public static void main(String[] args) {
		// execute(simpleAdd());
		// execute(fib());
		// execute(list());

		parseAndExecute("{Show 3 + 2}");
		parseAndExecute("local A=3 B=2 in {Show A + B} end");
		parseAndExecute("local fun {Inc Arg} Arg + 1 end in {Show {Inc 1}} end");
		parseAndExecute("local Fact in\nfun {Fact N} if N == 0 then 1 else N * {Fact N-1} end end\n{Show {Fact 30}}\nend");
		parseAndExecute("local L in L = [1 2 3] {Show L} {Show L.1} {Show L.2} end");
		parseAndExecute("local A in A=1+2 {Show A} end");
		parseAndExecute("local A B in A=B B=42 {Show B} {Show A} end");
	}

	private static void parseAndExecute(String code) {
		execute(new Translator().parseAndTranslate(code));
	}

	private static Object execute(OzRootNode rootNode) {
		return Truffle.getRuntime().createCallTarget(rootNode).call();
	}

	static OzRootNode simpleAdd() {
		// String code = "{Show 3 + 2}";

		OzNode left = new LongLiteralNode(3);
		OzNode right = new LongLiteralNode(2);
		OzNode addNode = AddNodeGen.create(left, right);
		OzNode showNode = ShowNodeGen.create(addNode);

		return new OzRootNode(new FrameDescriptor(), showNode);
	}

	static OzRootNode fib() {
		/*
		 * fun {Fact N}
		 *    if N == 0 then
		 *       1
		 *    else
		 *       N * {Fact N-1}
		 *    end
		 * end
		 * {Show {Fact 5}}
		 */

		FrameDescriptor topDescritor = new FrameDescriptor();
		FrameSlot fact = topDescritor.addFrameSlot("Fact");

		FrameDescriptor factDescriptor = new FrameDescriptor();
		FrameSlot n = factDescriptor.addFrameSlot("N");

		OzNode factPrelude = new BindVariableValueNode(n, new ReadArgumentNode());
		OzNode condition = EqualNodeGen.create(new ReadLocalVariableNode(n), new LongLiteralNode(0));
		OzNode thenExpr = new LongLiteralNode(1);
		OzNode nSub1 = SubNodeGen.create(new ReadLocalVariableNode(n), new LongLiteralNode(1));
		OzNode recur = CallFunctionNodeGen.create(new ReadCapturedVariableNode(fact), nSub1);
		OzNode elseExpr = MulNodeGen.create(new ReadLocalVariableNode(n), recur);
		OzNode ifNode = new IfNode(condition, thenExpr, elseExpr);
		OzNode factBody = new SequenceNode(factPrelude, ifNode);
		OzNode factDecl = new FunctionDeclarationNode(factDescriptor, factBody);
		OzNode declareFact = new BindVariableValueNode(fact, factDecl);

		OzNode callFact = CallFunctionNodeGen.create(new ReadLocalVariableNode(fact), new LongLiteralNode(30));
		OzNode showNode = ShowNodeGen.create(callFact);

		return new OzRootNode(topDescritor, new SequenceNode(declareFact, showNode));
	}

	static OzRootNode list() {
		/*
		 * L = [1 2 3]
		 * {Show L}
		 * {Show {Head L}}
		 * {Show {Tail L}}
		 */
		FrameDescriptor topDescritor = new FrameDescriptor();
		FrameSlot L = topDescritor.addFrameSlot("L");

		OzNode list = new ListLiteralNode(new OzNode[] { new LongLiteralNode(1), new LongLiteralNode(2), new LongLiteralNode(3) });
		OzNode writeL = new BindVariableValueNode(L, list);
		OzNode showL = ShowNodeGen.create(new ReadLocalVariableNode(L));

		return new OzRootNode(topDescritor, new SequenceNode(writeL, showL));
	}
}
