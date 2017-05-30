package org.mozartoz.truffle.runtime;

/**
 * An identity pair class to be used in general unification and equality
 * algorithms. Allows recognizing identical pairs of objects within collections.
 */
public final class IdentityPair implements Comparable<IdentityPair> {
	private final Object a, b;
	private final int hashCode;

	public IdentityPair(Object a, Object b) {
		int ha = System.identityHashCode(a);
		int hb = System.identityHashCode(b);
		if (ha < hb) {
			this.a = a;
			this.b = b;
			this.hashCode = 31 * ha + hb;
		} else {
			this.a = b;
			this.b = a;
			this.hashCode = 31 * hb + ha;
		}
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
