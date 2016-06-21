package org.mozartoz.truffle.nodes.control;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

public class NoElseNode extends OzNode {

	@Child OzNode valueNode;

	public NoElseNode(OzNode valueNode) {
		this.valueNode = valueNode;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object value = valueNode.execute(frame);
		SourceSection sourceSection = getSourceSection();
		throw kernelError("noElse",
				sourceSection.getSource().getName(),
				sourceSection.getStartLine(),
				value);
	}

}
