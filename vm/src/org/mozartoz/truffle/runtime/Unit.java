package org.mozartoz.truffle.runtime;

public class Unit {

	public static final Unit INSTANCE = new Unit();

	private Unit() {
	}

	@Override
	public String toString() {
		return "unit";
	}
}
