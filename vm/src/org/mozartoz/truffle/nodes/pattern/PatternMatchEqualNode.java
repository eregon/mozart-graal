package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltins.EqualNode;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class PatternMatchEqualNode extends OzNode {

	private final Object constant;

	@Child EqualNode equalNode = EqualNode.create();

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNode.create(value);
	}

	public PatternMatchEqualNode(Object constant) {
		this.constant = constant;
	}

	@Specialization
	boolean patternMatch(Object value) {
		return equalNode.executeEqual(value, constant);
	}

}
