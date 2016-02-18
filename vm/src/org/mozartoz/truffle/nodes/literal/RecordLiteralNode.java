package org.mozartoz.truffle.nodes.literal;

import java.util.Map;

import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.ExecuteValuesNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.ArrayUtils;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public class RecordLiteralNode extends OzNode {

	final Arity arity;
	final DynamicObjectFactory factory;
	@Child ExecuteValuesNode executeValuesNode;

	public RecordLiteralNode(Arity arity, OzNode[] values) {
		assert arity.getSize() == values.length;
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

	public static DynamicObject buildRecord(Arity arity, Object[] values) {
		assert values.length != 0;
		Object[] initialValues = ArrayUtils.unshift(arity.getLabel(), values);
		return arity.getShape().createFactory().newInstance(initialValues);
	}

	public static DynamicObject buildRecord(Object label, Map<?, ?> map) {
		Object[] features = map.keySet().toArray();
		Arity arity = Arity.build(label, features);
		Object[] values = map.values().toArray();
		return buildRecord(arity, values);
	}

}
