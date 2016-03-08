package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

@TypeSystemReference(OzTypes.class)
@ImportStatic(OzGuards.class)
public abstract class OzNode extends Node {

	protected static final Object unit = Unit.INSTANCE;

	public abstract Object execute(VirtualFrame frame);

	protected Object unimplemented() {
		throw new OzException(this, "unimplemented");
	}

	// Exception helpers

	private static final DynamicObjectFactory KERNEL_ERROR_FACTORY2 = Arity.build("kernel", 1L, 2L).createFactory();

	protected OzException kernelError(String kind, Object arg) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY2.newInstance("kernel", kind, arg));
		return new OzException(this, data);
	}

	private static final DynamicObjectFactory KERNEL_ERROR_FACTORY3 = Arity.build("kernel", 1L, 2L, 3L).createFactory();

	protected OzException kernelError(String kind, Object arg1, Object arg2) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY3.newInstance("kernel", kind, arg1, arg2));
		return new OzException(this, data);
	}

}
