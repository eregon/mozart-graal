package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class PatternMatchIdentityNode extends OzNode {

	private final Object constant;

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNode.create(value);
	}

	public PatternMatchIdentityNode(Object constant) {
		this.constant = constant;
	}

	@Specialization
	boolean patternMatch(Object value) {
		return constant == value;
	}

}
