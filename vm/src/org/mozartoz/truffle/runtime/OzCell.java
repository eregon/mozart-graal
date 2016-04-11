package org.mozartoz.truffle.runtime;

public class OzCell {

	Object value;

	public OzCell(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object exchange(Object newValue) {
		Object oldValue = value;
		value = newValue;
		return oldValue;
	}

	@Override
	public String toString() {
		return "<Cell " + value + ">";
	}

}
