package org.mozartoz.truffle.nodes.literal;

import java.util.HashMap;
import java.util.Map;

import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.ExecuteValuesNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzRecord;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;

public class MakeDynamicRecordNode extends OzNode {

	@Child OzNode labelNode;
	@Child ExecuteValuesNode featuresNode;
	@Child ExecuteValuesNode valuesNode;

	public MakeDynamicRecordNode(OzNode label, OzNode[] features, OzNode[] values) {
		assert features.length == values.length;
		this.labelNode = DerefNodeGen.create(label);
		this.featuresNode = new ExecuteValuesNode(derefIfBound(features));
		this.valuesNode = new ExecuteValuesNode(derefIfBound(values));
	}

	public OzNode getLabel() {
		return labelNode;
	}

	public OzNode[] getFeatures() {
		return featuresNode.getValues();
	}

	public OzNode[] getValues() {
		return valuesNode.getValues();
	}

	static OzNode[] derefIfBound(OzNode[] values) {
		OzNode[] deref = new OzNode[values.length];
		for (int i = 0; i < values.length; i++) {
			deref[i] = DerefIfBoundNodeGen.create(values[i]);
		}
		return deref;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object label = labelNode.execute(frame);
		Object[] features = featuresNode.executeValues(frame);
		Object[] values = valuesNode.executeValues(frame);

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
