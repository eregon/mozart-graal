package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

public class OzRootNode extends RootNode {

	public static OzRootNode createTopRootNode(String name, OzNode body) {
		final TopLevelHandlerNode handler = new TopLevelHandlerNode(body);
		return new OzRootNode(Loader.MAIN_SOURCE_SECTION, name, new FrameDescriptor(), handler, 0, false);
	}

	private final String name;
	private final int arity;
	private final boolean forceSplitting;

	@Child OzNode body;

	public OzRootNode(SourceSection sourceSection, String name, FrameDescriptor frameDescriptor, OzNode body, int arity, boolean forceSplitting) {
		super(OzLanguage.class, sourceSection, frameDescriptor);
		assert sourceSection != null;
		this.name = name;
		this.body = body;
		this.arity = arity;
		this.forceSplitting = forceSplitting;

		// Mark the body with the RootTag and make it has a source section
		body.hasRootTag = true;
		if (body.getSourceSection() == null) {
			body.setSourceSection(sourceSection);
		}
	}

	@Override
	public boolean isCloningAllowed() {
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	public OzNode getBody() {
		return body;
	}

	public boolean isForceSplitting() {
		return forceSplitting;
	}

	public RootCallTarget toCallTarget() {
		return Truffle.getRuntime().createCallTarget(this);
	}

	@Override
	protected boolean isInstrumentable() {
		return true;
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
		return OzBacktrace.formatNode(this);
	}

}
