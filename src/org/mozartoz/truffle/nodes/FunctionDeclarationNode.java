package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzFunction;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;

public class FunctionDeclarationNode extends OzNode {

	private final CallTarget callTarget;

	public FunctionDeclarationNode(FrameDescriptor frameDescriptor, OzNode body) {
		OzRootNode rootNode = new OzRootNode(frameDescriptor, body);
		this.callTarget = Truffle.getRuntime().createCallTarget(rootNode);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return new OzFunction(callTarget, frame.materialize());
	}

}
