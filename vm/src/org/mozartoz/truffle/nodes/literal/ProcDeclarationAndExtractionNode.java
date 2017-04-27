package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.local.CopyVariableToFrameNode;
import org.mozartoz.truffle.runtime.ArrayUtils;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class ProcDeclarationAndExtractionNode extends OzNode {

	private final RootCallTarget callTarget;
	private final FrameDescriptor capturedDescriptor;
	@Children final CopyVariableToFrameNode[] captureNodes;
	private final int arity;

	public ProcDeclarationAndExtractionNode(RootCallTarget callTarget, FrameDescriptor capturedDescriptor, CopyVariableToFrameNode[] captureNodes) {
		this.callTarget = callTarget;
		this.capturedDescriptor = capturedDescriptor;
		this.captureNodes = captureNodes;
		this.arity = ((OzRootNode) callTarget.getRootNode()).getArity();
	}

	@Override
	@ExplodeLoop
	public Object execute(VirtualFrame frame) {
		MaterializedFrame capture = Truffle.getRuntime().createMaterializedFrame(ArrayUtils.EMPTY, capturedDescriptor);
		for (int i = 0; i < captureNodes.length; i++) {
			captureNodes[i].executeWrite(frame, capture);
		}
		return new OzProc(callTarget, capture, arity);
	}

}
