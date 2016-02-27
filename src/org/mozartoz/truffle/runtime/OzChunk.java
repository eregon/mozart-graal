package org.mozartoz.truffle.runtime;

public class OzChunk {

	private final Object underlying;

	public OzChunk(Object underlying) {
		this.underlying = underlying;
	}

	public Object getUnderlying() {
		return underlying;
	}

	@Override
	public String toString() {
		return "<Chunk " + underlying + ">";
	}

}
