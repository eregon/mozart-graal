package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;

public class LongLiteralNode extends OzNode {

	private final long value;

	public LongLiteralNode(long value) {
		this.value = value;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return value;
	}

}
