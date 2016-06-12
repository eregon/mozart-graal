package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class ExecuteValuesNode extends OzNode {

	@Children final OzNode[] values;

	public ExecuteValuesNode(OzNode[] values) {
		this.values = values;
	}

	public OzNode[] getValues() {
		return values;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return executeValues(frame);
	}

	@ExplodeLoop
	public Object[] executeValues(VirtualFrame frame) {
		Object[] array = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			array[i] = values[i].execute(frame);
		}
		return array;
	}

}
