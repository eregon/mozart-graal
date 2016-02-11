package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.nodes.builtins.EqualNode;
import org.mozartoz.truffle.nodes.builtins.EqualNodeGen;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class PatternMatchEqualNode extends OzNode {

	private final Object constant;

	@Child EqualNode equalNode = EqualNodeGen.create(null, null);

	public PatternMatchEqualNode(Object constant) {
		this.constant = constant;
	}

	@Specialization
	boolean patternMatch(Object value) {
		return equalNode.executeEqual(value, constant);
	}

}
