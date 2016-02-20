package org.mozartoz.truffle.runtime;

public class OzUniqueName {

	private String name;

	public OzUniqueName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "<UniqueName " + name + ">";
	}

}
