package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ProcDeclarationNode extends OzNode {

	private final RootCallTarget callTarget;
	private final int arity;

	public ProcDeclarationNode(RootCallTarget callTarget) {
		this.callTarget = callTarget;
		this.arity = ((OzRootNode) callTarget.getRootNode()).getArity();
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return new OzProc(callTarget, frame.materialize(), arity);
	}

}
