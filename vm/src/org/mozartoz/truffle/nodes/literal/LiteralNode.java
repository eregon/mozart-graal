package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class LiteralNode extends OzNode {

	private final Object value;

	public LiteralNode(Object value) {
		this.value = value;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return value;
	}

}
