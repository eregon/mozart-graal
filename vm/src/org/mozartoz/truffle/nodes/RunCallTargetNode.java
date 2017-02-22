package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.ArrayUtils;
import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;

public class RunCallTargetNode extends OzNode {

	final CallTarget target;

	public RunCallTargetNode(CallTarget target) {
		this.target = target;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return target.call(OzArguments.pack(null, ArrayUtils.EMPTY));
	}

}
