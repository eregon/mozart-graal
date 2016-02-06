package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.frame.VirtualFrame;

public class UnboundLiteralNode extends OzNode {

	@Override
	public Object execute(VirtualFrame frame) {
		return new OzVar();
	}

}
