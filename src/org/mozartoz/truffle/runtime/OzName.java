package org.mozartoz.truffle.runtime;

public class OzName implements Comparable<OzName> {

	private static long currentID = 0;

	private final long id;

	public OzName() {
		this.id = ++currentID;
	}

	@Override
	public String toString() {
		return "<N>";
	}

	@Override
	public int compareTo(OzName other) {
		return Long.compare(this.id, other.id);
	}

}
