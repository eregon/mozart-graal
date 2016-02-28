package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.object.DynamicObject;

public class OzObject {

	private final DynamicObject clazz;
	private final DynamicObject features;

	public OzObject(DynamicObject clazz, DynamicObject features) {
		this.clazz = clazz;
		this.features = features;
	}

	public DynamicObject getClazz() {
		return clazz;
	}

	public DynamicObject getFeatures() {
		return features;
	}

}
