package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzError;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

@TypeSystemReference(OzTypes.class)
@ImportStatic(OzGuards.class)
public abstract class OzNode extends Node {

	protected static final Object unit = Unit.INSTANCE;

	public abstract Object execute(VirtualFrame frame);

	protected Object unimplemented() {
		throw new OzError(this, "unimplemented");
	}

}
