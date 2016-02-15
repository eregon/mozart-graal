package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class SkipNode extends OzNode {
	@Override
	public Object execute(VirtualFrame frame) {
		return unit;
	}
}
