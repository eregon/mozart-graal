package org.mozartoz.truffle.runtime;

public class OzString {

	private final String chars;

	public OzString(String chars) {
		this.chars = chars;
	}

	public String getChars() {
		return chars;
	}

	@Override
	public String toString() {
		return "<String " + chars + ">";
	}

}
