package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;

public class SkipNode extends OzNode {
	@Override
	public Object execute(VirtualFrame frame) {
		return unit;
	}
}
