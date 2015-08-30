package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public abstract class OzNode extends Node {

	public abstract Object execute(VirtualFrame frame);

}
