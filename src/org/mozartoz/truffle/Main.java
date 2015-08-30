package org.mozartoz.truffle;

import org.mozartoz.truffle.nodes.AddNode;
import org.mozartoz.truffle.nodes.AddNodeGen;
import org.mozartoz.truffle.nodes.LongLiteralNode;
import org.mozartoz.truffle.nodes.OzRootNode;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;

public class Main {
	public static void main(String[] args) {
		// String code = "{Show 3 + 2}";

		LongLiteralNode left = new LongLiteralNode(3);
		LongLiteralNode right = new LongLiteralNode(2);
		AddNode addNode = AddNodeGen.create(left, right);

		OzRootNode rootNode = new OzRootNode(addNode);
		CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

		Object value = callTarget.call();

		System.out.println(value);
	}
}
