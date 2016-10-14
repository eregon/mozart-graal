package org.mozartoz.truffle.runtime;

public final class OzFailedValue {

	private final Object data;

	public OzFailedValue(Object data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	@Override
	public String toString() {
		return "<Failed " + data + ">";
	}

}
