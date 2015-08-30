package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

public class OzRootNode extends RootNode {

	@Child OzNode body;

	public OzRootNode(FrameDescriptor frameDescriptor, OzNode body) {
		super(null, frameDescriptor);
		this.body = body;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return body.execute(frame);
	}

}
