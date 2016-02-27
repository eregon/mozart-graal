package org.mozartoz.truffle.runtime;

public class OzCell {

	Object value;

	public OzCell(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "<Cell " + value.toString() + ">";
	}

}
