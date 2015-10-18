package org.mozartoz.truffle.runtime;

public class Nil {

	public static final Nil INSTANCE = new Nil();

	private Nil() {
	}

	@Override
	public String toString() {
		return "nil";
	}
}
