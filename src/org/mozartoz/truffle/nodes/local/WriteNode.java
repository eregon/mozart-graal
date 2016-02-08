package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.frame.Frame;

public interface WriteNode {

	public Object executeWrite(Frame frame, Object value);

}
