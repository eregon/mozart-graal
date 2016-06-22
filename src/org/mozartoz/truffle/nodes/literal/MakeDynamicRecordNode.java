package org.mozartoz.truffle.nodes.literal;

import java.util.HashMap;
import java.util.Map;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.NodeHelpers;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzRecord;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;

public class MakeDynamicRecordNode extends OzNode {

	@Child OzNode labelNode;
	@Children final OzNode[] featureNodes;
	@Children final OzNode[] valueNodes;

	public MakeDynamicRecordNode(OzNode label, OzNode[] features, OzNode[] values) {
		assert features.length == values.length;
		this.labelNode = DerefNode.create(label);
		this.featureNodes = NodeHelpers.deref(features);
		this.valueNodes = NodeHelpers.derefIfBound(values);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object label = labelNode.execute(frame);
		Object[] features = NodeHelpers.executeValues(frame, featureNodes);
		Object[] values = NodeHelpers.executeValues(frame, valueNodes);

		return makeRecord(label, features, values);
	}

	@TruffleBoundary
	private Object makeRecord(Object label, Object[] features, Object[] values) {
		Map<Object, Object> map = new HashMap<Object, Object>(features.length);

		for (int i = 0; i < features.length; i++) {
			map.put(features[i], values[i]);
		}

		if (label == "|" && features.length == 2 && map.containsKey(1L) && map.containsKey(2L)) {
			return new OzCons(map.get(1L), map.get(2L));
		} else {
			return OzRecord.buildRecord(label, map);
		}

	}

}
