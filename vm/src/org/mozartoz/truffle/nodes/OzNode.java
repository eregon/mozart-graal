package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.RecordFactory;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@TypeSystemReference(OzTypes.class)
@ImportStatic(OzGuards.class)
public abstract class OzNode extends Node {

	private @CompilationFinal SourceSection sourceSection;

	public void setSourceSection(SourceSection sourceSection) {
		assert this.sourceSection == null;
		CompilerDirectives.transferToInterpreterAndInvalidate();
		this.sourceSection = sourceSection;
	}

	@Override
	public SourceSection getSourceSection() {
		return sourceSection;
	}

	protected static final Object unit = Unit.INSTANCE;

	public abstract Object execute(VirtualFrame frame);

	protected Object unimplemented() {
		throw new OzException(this, "unimplemented");
	}

	// Exception helpers

	private static final RecordFactory KERNEL_ERROR_FACTORY2 = Arity.build("kernel", 1L, 2L).createFactory();

	protected OzException kernelError(String kind, Object arg) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY2.newRecord(kind, arg));
		return new OzException(this, data);
	}

	private static final RecordFactory KERNEL_ERROR_FACTORY3 = Arity.build("kernel", 1L, 2L, 3L).createFactory();

	protected OzException kernelError(String kind, Object arg1, Object arg2) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY3.newRecord(kind, arg1, arg2));
		return new OzException(this, data);
	}

	private static final RecordFactory KERNEL_ERROR_FACTORY4 = Arity.build("kernel", 1L, 2L, 3L, 4L).createFactory();

	protected OzException kernelError(String kind, Object arg1, Object arg2, Object arg3) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY4.newRecord(kind, arg1, arg2, arg3));
		return new OzException(this, data);
	}

	private static final RecordFactory KERNEL_ERROR_FACTORY6 = Arity.build("kernel", 1L, 2L, 3L, 4L, 5L, 6L).createFactory();

	protected OzException kernelError(String kind, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY6.newRecord(kind, arg1, arg2, arg3, arg4, arg5));
		return new OzException(this, data);
	}
}
