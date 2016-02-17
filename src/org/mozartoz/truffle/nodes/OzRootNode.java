package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzLanguage;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

public class OzRootNode extends RootNode {

	@Child OzNode body;

	public OzRootNode(SourceSection sourceSection, FrameDescriptor frameDescriptor, OzNode body) {
		super(OzLanguage.class, sourceSection, frameDescriptor);
		this.body = body;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return body.execute(frame);
	}

}
