package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.ExecuteValuesNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.TailCallException;

import com.oracle.truffle.api.frame.VirtualFrame;

public class TailCallThrowerNode extends OzNode {

	private @Child OzNode receiver;
	private @Child ExecuteValuesNode arguments;

	public TailCallThrowerNode(OzNode receiver, ExecuteValuesNode arguments) {
		this.receiver = DerefNode.create(receiver);
		this.arguments = arguments;
	}

	public Object execute(VirtualFrame frame) {
		throw new TailCallException(receiver.execute(frame), arguments.execute(frame));
	}

}
