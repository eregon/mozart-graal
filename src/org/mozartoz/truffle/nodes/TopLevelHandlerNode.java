package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzError;

import com.oracle.truffle.api.frame.VirtualFrame;

public class TopLevelHandlerNode extends OzNode {

	@Child OzNode body;

	public TopLevelHandlerNode(OzNode body) {
		this.body = body;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		try {
			return body.execute(frame);
		} catch (OzError ozError) {
			System.err.println(ozError.getMessage());
			ozError.getBacktrace().showUserBacktrace();
			ozError.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return unit;
	}


}
