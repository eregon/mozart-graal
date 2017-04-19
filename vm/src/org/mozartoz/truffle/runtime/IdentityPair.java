package org.mozartoz.truffle.runtime;

/**
 * An identity pair class to be used in general unification and equality
 * algorithms. Allows recognizing identical pairs of objects within collections.
 */
public class IdentityPair implements Comparable<IdentityPair> {
	private final Object a, b;
	private final int hashCode;

	public IdentityPair(Object a, Object b) {
		if (a.hashCode() < b.hashCode()) {
			this.a = a;
			this.b = b;
		} else {
			this.a = b;
			this.b = a;
		}
		long hashCode64 = ((long) System.identityHashCode(this.a) << 32) | System.identityHashCode(this.b);
		this.hashCode = (int) ((hashCode64 >> 16) ^ hashCode64); // TODO improve distribution?
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IdentityPair)) {
			return false;
		}
		IdentityPair pair = (IdentityPair) obj;
		return this.a == pair.a && this.b == pair.b;
	}

	@Override
	public int compareTo(IdentityPair o) {
		return hashCode - o.hashCode;
	}

}
