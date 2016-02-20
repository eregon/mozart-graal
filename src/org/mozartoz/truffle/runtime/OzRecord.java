package org.mozartoz.truffle.runtime;

import java.util.Map;

import com.oracle.truffle.api.object.DynamicObject;

/**
 * An Oz Record is represented as a DynamicObject with RecordObjectType.
 */
public abstract class OzRecord {

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

	public static Arity getArity(DynamicObject record) {
		return new Arity(Arity.LABEL_LOCATION.get(record), record.getShape());
	}

}
