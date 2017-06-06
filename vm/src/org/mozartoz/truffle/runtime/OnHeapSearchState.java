package org.mozartoz.truffle.runtime;

import java.util.HashSet;
import java.util.LinkedList;

public class OnHeapSearchState {

	public boolean root = true;
	public HashSet<IdentityPair> encountered = new HashSet<>();
	public LinkedList<IdentityPair> toExplore = new LinkedList<>();

}
