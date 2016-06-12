package org.mozartoz.truffle.runtime;

import java.util.HashMap;
import java.util.Map;

public class OzUniqueName implements Comparable<OzUniqueName> {

	private static final Map<String, OzUniqueName> TABLE = new HashMap<>();

	private String name;

	public static OzUniqueName get(String name) {
		return TABLE.computeIfAbsent(name, OzUniqueName::new);
	}

	private OzUniqueName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int compareTo(OzUniqueName other) {
		return name.compareTo(other.name);
	}

	@Override
	public String toString() {
		return "<N: " + name + ">";
	}

}
