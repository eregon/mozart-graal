package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.Source;
import org.mozartoz.truffle.runtime.OzLanguage;

public class RunMainNode extends OzNode {

	final Source source;

	public RunMainNode(Source source) {
		this.source = source;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		OzLanguage.getContext().run(source);
		return unit;
	}

}
