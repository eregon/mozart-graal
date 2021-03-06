package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild("value")
public abstract class PatternMatchRecordNode extends OzNode {

	final Arity arity;

	public PatternMatchRecordNode(Arity arity) {
		this.arity = arity;
	}

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNode.create(value);
	}

	@Specialization(guards = "arity.matches(record)")
	boolean patternMatch(DynamicObject record) {
		return true;
	}

	@Specialization(guards = "!arity.matches(record)")
	boolean patternNoMatch(DynamicObject record) {
		return false;
	}

	@Specialization(guards = "!isRecord(object)")
	boolean patternMatch(Object object) {
		return false;
	}

}
