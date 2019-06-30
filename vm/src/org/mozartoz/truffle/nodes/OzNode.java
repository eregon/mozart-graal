package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.instrumentation.Tag;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.Errors;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.RecordFactory;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@TypeSystemReference(OzTypes.class)
@ImportStatic(OzGuards.class)
@GenerateWrapper
public abstract class OzNode extends Node implements InstrumentableNode {

	boolean hasRootTag = false;

	private @CompilationFinal SourceSection sourceSection;

	public void setSourceSection(SourceSection sourceSection) {
		CompilerDirectives.transferToInterpreterAndInvalidate();
		this.sourceSection = sourceSection;
	}

	@Override
	public SourceSection getSourceSection() {
		return sourceSection;
	}

	protected static final Object unit = Unit.INSTANCE;

	public abstract Object execute(VirtualFrame frame);

	@Override
	public boolean hasTag(Class<? extends Tag> tag) {
		if (tag == RootTag.class) {
			return hasRootTag;
		}
		return false;
	}

	@Override
	public WrapperNode createWrapper(ProbeNode probeNode) {
		return new OzNodeWrapper(this, probeNode);
	}

	@Override
	public boolean isInstrumentable() {
		return true;
	}

	protected Object unimplemented() {
		throw new OzException(this, "unimplemented");
	}

}
