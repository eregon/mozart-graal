package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.EqualNode;
import org.mozartoz.truffle.nodes.builtins.EqualNodeGen;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class PatternMatchEqualNode extends OzNode {

	private final Object constant;

	@Child EqualNode equalNode = EqualNodeGen.create(null, null);

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNodeGen.create(value);
	}

	public PatternMatchEqualNode(Object constant) {
		this.constant = constant;
	}

	@Specialization
	boolean patternMatch(Object value) {
		return equalNode.executeEqual(value, constant);
	}

}
