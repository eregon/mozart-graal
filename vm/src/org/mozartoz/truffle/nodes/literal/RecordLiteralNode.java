package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.ExecuteValuesNode;
import org.mozartoz.truffle.runtime.ArrayUtils;
import org.mozartoz.truffle.runtime.OzArity;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public class RecordLiteralNode extends OzNode {

	final OzArity arity;
	final DynamicObjectFactory factory;
	@Child ExecuteValuesNode executeValuesNode;

	public RecordLiteralNode(OzArity arity, OzNode[] values) {
		this.arity = arity;
		this.factory = arity.getShape().createFactory();
		this.executeValuesNode = new ExecuteValuesNode(derefIfBound(values));
	}

	static OzNode[] derefIfBound(OzNode[] values) {
		OzNode[] deref = new OzNode[values.length];
		for (int i = 0; i < values.length; i++) {
			deref[i] = DerefIfBoundNodeGen.create(values[i]);
		}
		return deref;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object[] values = executeValuesNode.executeValues(frame);
		Object[] initialValues = ArrayUtils.unshift(arity.getLabel(), values);
		return factory.newInstance(initialValues);
	}



}
