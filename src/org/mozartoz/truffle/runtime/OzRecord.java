package org.mozartoz.truffle.runtime;

import java.util.Map;

import com.oracle.truffle.api.object.DynamicObject;

/**
 * An Oz Record is represented as a DynamicObject with RecordObjectType.
 */
public abstract class OzRecord {

	public static DynamicObject buildRecord(Arity arity, Object... values) {
		assert values.length != 0;
		assert !arity.isConsArity();
		Object[] initialValues = ArrayUtils.unshift(arity.getLabel(), values);
		return arity.createFactory().newInstance(initialValues);
	}

	public static DynamicObject buildRecord(Object label, Map<?, ?> map) {
		Object[] features = map.keySet().toArray();
		Arity.sortFeaturesInPlace(features);
		Arity arity = Arity.build(label, features);

		Object[] values = new Object[features.length];
		int i = 0;
		for (Object feature : features) {
			values[i++] = map.get(feature);
		}

		return buildRecord(arity, values);
	}

	public static Object getLabel(DynamicObject record) {
		return Arity.LABEL_LOCATION.get(record);
	}

	public static Arity getArity(DynamicObject record) {
		return Arity.forRecord(record);
	}

}
