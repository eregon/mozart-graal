package org.mozartoz.truffle.runtime;

/**
 * An identity pair class to be used in general unification and equality
 * algorithms. Allows recognizing identical pairs of objects within collections.
 */
public class IdentityPair implements Comparable<IdentityPair> {
	private final Object a, b;
	private final int hashCode;

	public IdentityPair(Object a, Object b) {
		int ha = System.identityHashCode(a);
		int hb = System.identityHashCode(b);
		long hashCode64;
		if (ha < hb) {
			this.a = a;
			this.b = b;
			hashCode64 = ((long) ha << 32) | hb;
		} else {
			this.a = b;
			this.b = a;
			hashCode64 = ((long) hb << 32) | ha;
		}
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
