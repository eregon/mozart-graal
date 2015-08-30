package org.mozartoz.truffle;

import org.mozartoz.truffle.nodes.AddNode;
import org.mozartoz.truffle.nodes.AddNodeGen;
import org.mozartoz.truffle.nodes.LongLiteralNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.ShowNode;
import org.mozartoz.truffle.nodes.ShowNodeGen;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;

public class Main {
	public static void main(String[] args) {
		// String code = "{Show 3 + 2}";

		LongLiteralNode left = new LongLiteralNode(3);
		LongLiteralNode right = new LongLiteralNode(2);
		AddNode addNode = AddNodeGen.create(left, right);
		ShowNode showNode = ShowNodeGen.create(addNode);

		OzRootNode rootNode = new OzRootNode(showNode);
		CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

		callTarget.call();
	}
}
