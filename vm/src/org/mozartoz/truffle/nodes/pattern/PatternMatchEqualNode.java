package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.DFSEqualNode;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class PatternMatchEqualNode extends OzNode {

	private final Object constant;

	@Child DFSEqualNode equalNode = DFSEqualNode.create();

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNode.create(value);
	}

	public static OzNode create(Object constant, OzNode value) {
		if (OzGuards.hasReferenceEquality(constant)) {
			return PatternMatchIdentityNodeGen.create(constant, value);
		} else {
			return PatternMatchEqualNodeGen.create(constant, value);
		}
	}

	public PatternMatchEqualNode(Object constant) {
		this.constant = constant;
	}

	@Specialization
	boolean patternMatch(Object value) {
		return equalNode.executeEqual(constant, value, null);
	}

}
