package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.runtime.OzFunction;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

public class ProcDeclarationNode extends OzNode {

	private final CallTarget callTarget;

	public ProcDeclarationNode(SourceSection sourceSection, FrameDescriptor frameDescriptor, OzNode body) {
		assignSourceSection(sourceSection);
		OzRootNode rootNode = new OzRootNode(sourceSection, frameDescriptor, body);
		this.callTarget = Truffle.getRuntime().createCallTarget(rootNode);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return new OzFunction(callTarget, frame.materialize());
	}

}