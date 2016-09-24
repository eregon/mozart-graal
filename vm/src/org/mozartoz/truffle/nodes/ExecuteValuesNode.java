package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ExecuteValuesNode extends OzNode {

	@Children final OzNode[] valuesNodes;

	public ExecuteValuesNode(OzNode[] valuesNodes) {
		this.valuesNodes = valuesNodes;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return NodeHelpers.executeValues(frame, valuesNodes);
	}

}
