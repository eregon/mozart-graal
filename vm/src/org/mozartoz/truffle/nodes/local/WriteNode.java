package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInterface;

public interface WriteNode extends NodeInterface {

	public Object executeWrite(VirtualFrame topFrame, Object value);

}
