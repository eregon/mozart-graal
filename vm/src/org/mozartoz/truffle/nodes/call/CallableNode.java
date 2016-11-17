package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class CallableNode extends OzNode {

	public abstract Object executeCall(VirtualFrame frame, Object receiver, Object[] arguments);

}
