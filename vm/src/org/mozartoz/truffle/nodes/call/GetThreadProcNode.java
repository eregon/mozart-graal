package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzThread;

import com.oracle.truffle.api.frame.VirtualFrame;

public class GetThreadProcNode extends OzNode {

	@Override
	public Object execute(VirtualFrame frame) {
		return OzThread.getCurrent().getAndClearInitialProc();
	}


}
