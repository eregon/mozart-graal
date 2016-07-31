package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public class RecordFactory {

	private final Object label;
	private final DynamicObjectFactory factory;

	RecordFactory(Object label, DynamicObjectFactory factory) {
		this.label = label;
		this.factory = factory;
	}

	public DynamicObject newRecord(Object... values) {
		Object[] initialValues = ArrayUtils.unshift(label, values);
		return factory.newInstance(initialValues);
	}

}
