package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class PatternMatchAtomNode extends OzNode {

	private final String atom;

	public PatternMatchAtomNode(String atom) {
		this.atom = atom;
	}

	@Specialization
	boolean patternMatch(Object value) {
		return value == atom;
	}

}
