package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.object.DynamicObject;

public class OzObject extends OzValue {

	private final DynamicObject clazz;
	private final DynamicObject features;
	private final DynamicObject attributes;

	public OzObject(DynamicObject clazz, DynamicObject features, DynamicObject attributes) {
		this.clazz = clazz;
		this.features = features;
		this.attributes = attributes;
	}

	public DynamicObject getClazz() {
		return clazz;
	}

	public DynamicObject getFeatures() {
		assert features != null;
		return features;
	}

	public DynamicObject getAttributes() {
		assert attributes != null;
		return attributes;
	}

	@Override
	public String toString() {
		return "<O>"; // attr=" + attributes + " feat=" + features + ">";
	}

}
