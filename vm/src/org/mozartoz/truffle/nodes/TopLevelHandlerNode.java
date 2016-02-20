package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzException;

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
		} catch (OzException ozException) {
			System.err.println(ozException.getMessage());
			ozException.getBacktrace().showUserBacktrace();
			ozException.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return unit;
	}


}
