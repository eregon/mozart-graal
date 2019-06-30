package org.mozartoz.truffle.nodes.control;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.mozartoz.truffle.runtime.Errors;

public class NoElseNode extends OzNode {

	@Child OzNode valueNode;

	public NoElseNode(OzNode valueNode) {
		this.valueNode = valueNode;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object value = valueNode.execute(frame);
		SourceSection sourceSection = getSourceSection();
		throw Errors.kernelError(this, "noElse", sourceSection.getSource().getName(), (long) getStartLine(sourceSection), value);
	}

	@TruffleBoundary
	private int getStartLine(SourceSection sourceSection) {
		return sourceSection.getStartLine();
	}

}
