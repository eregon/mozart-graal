package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.object.DynamicObject;

public class OzChunk extends OzValue {

	private final DynamicObject underlying;

	public OzChunk(DynamicObject underlying) {
		this.underlying = underlying;
	}

	public DynamicObject getUnderlying() {
		return underlying;
	}

	@Override
	public String toString() {
		return "<Chunk>";
	}

}
