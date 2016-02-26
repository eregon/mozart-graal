package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild("value")
public abstract class PatternMatchOpenRecordNode extends OzNode {

	final Arity arity;

	public PatternMatchOpenRecordNode(Arity arity) {
		this.arity = arity;
	}

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNodeGen.create(value);
	}

	@Specialization(guards = "arity.matchesOpen(record)")
	boolean patternMatchOpen(DynamicObject record) {
		return true;
	}

	@Specialization(guards = "!isRecord(object)")
	boolean patternMatchOpen(Object object) {
		return false;
	}

}
