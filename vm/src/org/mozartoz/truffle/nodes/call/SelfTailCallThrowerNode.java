package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.SelfTailCallException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class SelfTailCallThrowerNode extends OzNode {

	@Children final OzNode[] arguments;

	public SelfTailCallThrowerNode(OzNode[] arguments) {
		this.arguments = arguments;
	}

	public Object execute(VirtualFrame frame) {
		replaceArguments(frame);
		throw SelfTailCallException.INSTANCE;
	}

	@ExplodeLoop
	public void replaceArguments(VirtualFrame frame) {
		for (int i = 0; i < arguments.length; i++) {
			OzArguments.setArgument(frame, i, arguments[i].execute(frame));
		}
	}

}
