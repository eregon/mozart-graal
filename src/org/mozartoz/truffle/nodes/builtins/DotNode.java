package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

@NodeChildren({ @NodeChild("record"), @NodeChild("feature") })
public abstract class DotNode extends OzNode {

	@CreateCast("record")
	protected OzNode derefRecord(OzNode var) {
		return DerefNodeGen.create(var);
	}

	@Specialization(guards = "feature == 1")
	protected Object getHead(OzCons cons, long feature) {
		return cons.getHead();
	}

	@Specialization(guards = "feature == 2")
	protected Object getTail(OzCons cons, long feature) {
		return cons.getTail();
	}

	@Specialization(guards = {
			"feature == cachedFeature",
			"record.getShape() == cachedShape"
	})
	protected Object getRecord(DynamicObject record, Object feature,
			@Cached("feature") Object cachedFeature,
			@Cached("record.getShape()") Shape cachedShape,
			@Cached("cachedShape.getProperty(cachedFeature)") Property property) {
		return property.get(record, cachedShape);
	}

}
