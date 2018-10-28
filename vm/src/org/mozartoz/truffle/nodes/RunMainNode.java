package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.mozartoz.truffle.runtime.OzContext;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.Source;

public class RunMainNode extends OzNode {

	final Source source;

	public RunMainNode(Source source) {
		this.source = source;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		OzContext.getInstance().run(source);
		return unit;
	}

}
