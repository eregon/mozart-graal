package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class BooleanLiteralNode extends OzNode {

	private final boolean value;

	public BooleanLiteralNode(boolean value) {
		this.value = value;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return value;
	}

}
