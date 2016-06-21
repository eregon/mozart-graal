package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.NodeHelpers;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.ArrayUtils;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public class RecordLiteralNode extends OzNode {

	final Arity arity;
	final DynamicObjectFactory factory;
	@Children final OzNode[] valueNodes;

	public RecordLiteralNode(Arity arity, OzNode[] values) {
		assert arity.getWidth() == values.length;
		this.arity = arity;
		this.factory = arity.getShape().createFactory();
		this.valueNodes = NodeHelpers.derefIfBound(values);
	}

	public Arity getArity() {
		return arity;
	}

	public OzNode[] getValues() {
		return valueNodes;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object[] values = NodeHelpers.executeValues(frame, valueNodes);
		Object[] initialValues = ArrayUtils.unshift(arity.getLabel(), values);
		return factory.newInstance(initialValues);
	}

}
