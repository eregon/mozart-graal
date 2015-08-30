package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadArgumentNode extends OzNode {

	@Override
	public Object execute(VirtualFrame frame) {
		return OzArguments.getArgument(frame);
	}

}
