package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzLanguage;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

public class OzRootNode extends RootNode {

	private final int arity;
	@Child OzNode body;

	public OzRootNode(SourceSection sourceSection, FrameDescriptor frameDescriptor, OzNode body, int arity) {
		super(OzLanguage.class, sourceSection, frameDescriptor);
		this.body = body;
		this.arity = arity;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		checkArity(frame);
		return body.execute(frame);
	}

	private void checkArity(VirtualFrame frame) {
		int given = OzArguments.getArgumentCount(frame);
		if (given != arity) {
			CompilerDirectives.transferToInterpreter();
			throw new OzException(this, "arity mismatch: " + given + " VS " + arity);
		}
	}

	@Override
	public String toString() {
		return OzRootNode.class.getSimpleName() + "@" + getSourceSection();
	}

}
