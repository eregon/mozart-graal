package org.mozartoz.truffle.nodes.pattern;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzRecord;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild("value")
public abstract class PatternMatchDynamicArityNode extends OzNode {

	@CompilationFinal(dimensions = 1) final Object[] features;

	public PatternMatchDynamicArityNode(Object[] features) {
		this.features = features;
	}

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNode.create(value);
	}

	@TruffleBoundary
	@Specialization
	boolean patternMatch(DynamicObject record) {
		Object label = OzRecord.getLabel(record);
		return Arity.build(label, features).matches(record);
	}

	@Specialization(guards = "!isRecord(object)")
	boolean patternMatch(Object object) {
		return false;
	}

}
