package org.mozartoz.truffle;

import org.mozartoz.truffle.nodes.AddNodeGen;
import org.mozartoz.truffle.nodes.CallFunctionNodeGen;
import org.mozartoz.truffle.nodes.EqualNodeGen;
import org.mozartoz.truffle.nodes.FunctionDeclarationNode;
import org.mozartoz.truffle.nodes.IfNode;
import org.mozartoz.truffle.nodes.LongLiteralNode;
import org.mozartoz.truffle.nodes.MulNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.ReadArgumentNode;
import org.mozartoz.truffle.nodes.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.SequenceNode;
import org.mozartoz.truffle.nodes.ShowNodeGen;
import org.mozartoz.truffle.nodes.SubNodeGen;
import org.mozartoz.truffle.nodes.WriteLocalVariableNode;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;

public class Main {
	public static void main(String[] args) {
		OzRootNode rootNode = fib(); // simpleAdd();
		CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
		callTarget.call();
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

		OzNode factPrelude = new WriteLocalVariableNode(n, new ReadArgumentNode());
		OzNode condition = EqualNodeGen.create(new ReadLocalVariableNode(n), new LongLiteralNode(0));
		OzNode thenExpr = new LongLiteralNode(1);
		OzNode nSub1 = SubNodeGen.create(new ReadLocalVariableNode(n), new LongLiteralNode(1));
		OzNode recur = CallFunctionNodeGen.create(new ReadCapturedVariableNode(fact), nSub1);
		OzNode elseExpr = MulNodeGen.create(new ReadLocalVariableNode(n), recur);
		OzNode ifNode = new IfNode(condition, thenExpr, elseExpr);
		OzNode factBody = new SequenceNode(factPrelude, ifNode);
		OzNode factDecl = new FunctionDeclarationNode(factDescriptor, factBody);
		OzNode declareFact = new WriteLocalVariableNode(fact, factDecl);

		OzNode callFact = CallFunctionNodeGen.create(new ReadLocalVariableNode(fact), new LongLiteralNode(30));
		OzNode showNode = ShowNodeGen.create(callFact);

		return new OzRootNode(topDescritor, new SequenceNode(declareFact, showNode));
	}
}
